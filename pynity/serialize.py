import io
import logging
import uuid

from .io import AutoCloseable, BinaryIO, ChunkedFileIO, ByteOrder
from .utils import ObjectDict
from .typedb import TypeDatabase, TypeException
from .stringtable import StringTable

METAFLAG_ALIGN = 0x4000

log = logging.getLogger("pynity.serialize")

class SerializedFile(AutoCloseable):

    versions = [5, 6, 8, 9, 14, 15]
    read_prim = {
        "bool":             BinaryIO.read_bool8,
        "SInt8":            BinaryIO.read_int8,
        "UInt8":            BinaryIO.read_uint8,
        "char":             BinaryIO.read_uint8,
        "SInt16":           BinaryIO.read_int16,
        "short":            BinaryIO.read_int16,
        "UInt16":           BinaryIO.read_uint16,
        "unsigned short":   BinaryIO.read_uint16,
        "SInt32":           BinaryIO.read_int32,
        "int":              BinaryIO.read_int32,
        "UInt32":           BinaryIO.read_uint32,
        "unsigned int":     BinaryIO.read_uint32,
        "SInt64":           BinaryIO.read_int64,
        "long":             BinaryIO.read_int64,
        "UInt64":           BinaryIO.read_uint64,
        "unsigned long":    BinaryIO.read_uint64,
        "float":            BinaryIO.read_float,
        "double":           BinaryIO.read_double,
    }

    types_cache = {}

    @classmethod
    def probe_path(cls, path):
        with ChunkedFileIO.open(path, "rb") as fp:
            return cls.probe_file(fp)

    @classmethod
    def probe_file(cls, file):
        r = BinaryIO(file, order=ByteOrder.BIG_ENDIAN)

        # get file size
        r.seek(0, io.SEEK_END)
        file_size = r.tell()
        r.seek(0, io.SEEK_SET)

        # check for minimum header size
        if file_size < 16:
            return False

        # read some parts of the header
        r.seek(4, io.SEEK_SET)
        header_file_size = r.read_int32()
        header_version = r.read_int32()
        r.seek(0, io.SEEK_SET)

        # check version range
        if not cls.versions[0] <= header_version <= cls.versions[-1]:
            return False

        # check file size
        return file_size == header_file_size

    def __init__(self, file):
        self.type_db = TypeDatabase()

        if isinstance(file, str):
            fp = ChunkedFileIO.open(file, "rb")
        else:
            fp = file

        self.r = BinaryIO(fp, order=ByteOrder.BIG_ENDIAN)

        # read metadata
        self._read_header()
        self._read_types()
        self._read_object_info()
        self._read_script_types()
        self._read_externals()

    def __iter__(self):
        for path_id in self.objects:
            obj = self.read_object(path_id)
            if obj:
                yield path_id, obj

    def _read_header(self, r=None):
        if not r:
            r = self.r

        header = self.header = ObjectDict()
        header.metadata_size = r.read_int32()
        header.file_size = r.read_int32()
        header.version = r.read_int32()
        header.data_offset = r.read_int32()

        if header.version not in self.versions:
            raise NotImplementedError("Unsupported format version: %d"
                                      % header.version)

        if header.data_offset > header.file_size:
            raise SerializedFileError("Invalid data offset: %d"
                                      % header.data_offset)

        if header.metadata_size > header.file_size:
            raise SerializedFileError("Invalid metadata size: %d"
                                      % header.metadata_size)

        # newer formats usually use little-endian for the rest of the file
        if header.version > 8:
            header.endianness = r.read_int8()
            r.read(3) # reserved
            r.order = ByteOrder(header.endianness)
        elif header.version > 5:
            r.order = ByteOrder.LITTLE_ENDIAN

    def _read_types(self, r=None):
        if not r:
            r = self.r

        types = self.types = ObjectDict()

        # older formats store the object data before the structure data
        if self.header.version < 9:
            types_offset = self.header.file_size - self.header.metadata_size + 1
            r.seek(types_offset)

        if self.header.version > 6:
            types.signature = r.read_cstring()
            types.attributes = r.read_int32()

        if self.header.version > 13:
            types.embedded = r.read_bool8()

        types.classes = {}
        types_raw = self.types_raw = {}

        num_classes = r.read_int32()
        for _ in range(num_classes):
            class_type = ObjectDict()
            class_id = r.read_int32()

            if self.header.version > 13:
                if class_id < 0:
                    class_type.script_id = r.read_hex(16)

                class_type.old_type_hash = r.read_hex(16)

                if types.embedded:
                    type_pos = r.tell()
                    class_type.type_tree = self._read_type_node()
                    type_size = r.tell() - type_pos

                    r.seek(type_pos)
                    types_raw[class_id] = r.read(type_size)
            else:
                type_pos = r.tell()
                class_type.type_tree = self._read_type_node_old()
                type_size = r.tell() - type_pos

                r.seek(type_pos)
                types_raw[class_id] = r.read(type_size)

            if class_id in types.classes:
                raise SerializedFileError("Duplicate class ID %d" % class_id)

            types.classes[class_id] = class_type

        # padding
        if 6 < self.header.version < 13:
            r.read_int32()

    def _read_type_node(self, r=None):
        if not r:
            r = self.r

        # read sizes
        num_fields = r.read_int32()
        string_table_len = r.read_int32()

        # read local string table first so the strings can be assigned in one go
        tree_pos = r.tell()
        tree_len = 24 * num_fields
        r.seek(tree_len, io.SEEK_CUR)
        string_table_buf = r.read(string_table_len)
        string_table = StringTable.load(string_table_buf)
        r.seek(tree_pos)

        # read type tree
        field_stack = []
        field_root = None

        for _ in range(num_fields):
            field = ObjectDict()
            field.version = r.read_int16()

            level = r.read_uint8()

            # pop redundant entries from stack if required
            while len(field_stack) > level:
                field_stack.pop()

            # add current node as child for topmost (previous) node
            if field_stack:
                field_stack[-1].children.append(field)

            # add current node on top of stack
            field_stack.append(field)

            field.is_array = r.read_bool8()

            # assign type string
            type_offset = r.read_uint32()
            field.type = string_table.get(type_offset)
            if not field.type:
                raise SerializedFileError("Invalid field type string offset: %d"
                                          % type_offset)

            # assign name string
            name_offset = r.read_uint32()
            field.name = string_table.get(name_offset)
            if not field.name:
                raise SerializedFileError("Invalid field name string offset: %d"
                                          % name_offset)

            field.size = r.read_int32()
            field.index = r.read_int32()
            field.meta_flag = r.read_int32()
            field.children = []

            # save first node, which is the root
            if not field_root:
                field_root = field

        # correct end position
        r.seek(string_table_len, io.SEEK_CUR)

        return field_root

    def _read_type_node_old(self, r=None):
        if not r:
            r = self.r

        field = ObjectDict()
        field.type = r.read_cstring()
        field.name = r.read_cstring()
        field.size = r.read_int32()
        field.index = r.read_int32()
        field.is_array = r.read_bool32()
        field.version = r.read_int32()
        field.meta_flag = r.read_int32()
        field.children = []

        num_children = r.read_int32()
        for _ in range(num_children):
            field.children.append(self._read_type_node_old(r))

        return field

    def _read_object_info(self, r=None):
        if not r:
            r = self.r

        objects = self.objects = {}

        num_entries = r.read_int32()

        for _ in range(num_entries):
            if self.header.version > 13:
                r.align(4)
                path_id = r.read_uint64()
            else:
                path_id = r.read_uint32()

            obj = ObjectDict()
            obj.byte_start = r.read_uint32()
            obj.byte_size = r.read_uint32()
            obj.type_id = r.read_int32()
            obj.class_id = r.read_int16()

            if obj.byte_start > self.header.file_size:
                raise SerializedFileError("Invalid byte start: %d" % obj.byte_start)

            if obj.byte_size > self.header.file_size:
                raise SerializedFileError("Invalid byte size: %d" % obj.byte_start)

            if self.header.version > 13:
                obj.script_type_index = r.read_int16()
            else:
                obj.is_destroyed = r.read_bool16()

            if self.header.version > 14:
                obj.stripped = r.read_bool8()

            if path_id in objects:
                raise SerializedFileError("Duplicate path ID: %d" % path_id)

            objects[path_id] = obj

    def _read_script_types(self, r=None):
        if not r:
            r = self.r

        script_types = self.script_types = []

        # script types exist in newer versions only
        if self.header.version < 11:
            return

        num_entries = r.read_int32()

        for _ in range(num_entries):
            r.align(4)

            script_type = ObjectDict()
            script_type.serialized_file_index = r.read_int32()
            script_type.identifier_in_file = r.read_int64()

            script_types.append(script_type)

    def _read_externals(self, r=None):
        if not r:
            r = self.r

        externals = self.externals = []

        num_entries = r.read_int32()
        for _ in range(num_entries):
            external = ObjectDict()

            if self.header.version > 5:
                external.asset_path = r.read_cstring()

            external.guid = uuid.UUID(bytes=r.read(16))
            external.type = r.read_int32()
            external.file_path = r.read_cstring()

            externals.append(external)

    def _read_object_node(self, obj_type, obj_end, r=None):
        if not r:
            r = self.r

        if log.isEnabledFor(logging.DEBUG):
            log.debug("%d %s %s", r.tell(), obj_type.type, obj_type.name)

        if obj_type.is_array:
            # unpack "Array" objects to native Python arrays
            type_size = obj_type.children[0]
            type_data = obj_type.children[1]

            size = self._read_object_node(type_size, obj_end)
            if type_data.type in ("SInt8", "UInt8", "char"):
                # fix size for AudioClips that are linked with .resS files
                if self.header.version <= 13:
                    size = min(size, obj_end - r.tell())

                # read byte array
                obj = r.read(size)
            else:
                # read generic array
                obj = []
                for _ in range(size):
                    obj.append(self._read_object_node(type_data, obj_end))

            # arrays always need to be aligned in version 5 or newer
            if self.header.version > 5:
                r.align(4)
        elif obj_type.size > 0 and not obj_type.children:
            # no children and size greater zero -> primitive
            if obj_type.type not in self.read_prim:
                raise SerializationError("Unknown primitive type: " + obj_type.type)

            obj = self.read_prim[obj_type.type](r)

            # align if flagged
            if obj_type.meta_flag & METAFLAG_ALIGN != 0:
                r.align(4)
        else:
            # complex object with children
            obj_class = self.types_cache.get(obj_type.type)
            if not obj_class:
                obj_class = type(obj_type.type, (ObjectDict,), {})
                self.types_cache[obj_type.type] = obj_class

            obj = obj_class()

            for child in obj_type.children:
                obj[child.name] = self._read_object_node(child, obj_end)

        if obj_type.type == "string":
            # convert string objects to native Python strings
            try:
                obj = obj.Array.decode("utf-8")
            except UnicodeDecodeError:
                # could be a TextAsset that contains binary data, return raw string
                log.warning("Can't decode string at %d as UTF-8, "
                            "using raw data instead", r.tell())
                obj = obj.Array
        elif obj_type.type == "vector":
            # unpack collection containers
            obj = obj.Array

        return obj

    def read_object(self, path_id, r=None):
        if not r:
            r = self.r

        # get object info
        obj_info = self.objects.get(path_id)
        if not obj_info:
            raise ValueError("Invalid path ID: %d" % path_id)

        # get object type class
        obj_class = self.types.classes.get(obj_info.type_id)

        # use embedded object type tree or load it from database otherwise
        if self.header.version > 13:
            if not self.types.embedded:
                # object_class should always be defined in newer formats
                assert obj_class

                try:
                    with self.type_db.open(obj_info.type_id,
                                           obj_class.old_type_hash) as fp:
                        obj_class.type_tree = self._read_type_node(fp)
                except TypeException as ex:
                    log.warning(ex)
        elif not obj_class:
            try:
                with self.type_db.open_old(obj_info.type_id,
                                           self.types.signature) as fp:
                    obj_class = ObjectDict()
                    obj_class.type_tree = self._read_type_node_old(fp)
                    self.types.classes[obj_info.type_id] = obj_class
            except TypeException as ex:
                log.warning(ex)

        # cancel if there's no type tree available
        if not obj_class or "type_tree" not in obj_class:
            return

        obj_type = obj_class.type_tree
        if not obj_type:
            return

        # seek to object data start position
        obj_pos = self.header.data_offset + obj_info.byte_start
        obj_end = obj_pos + obj_info.byte_size
        r.seek(obj_pos, io.SEEK_SET)

        # deserialize all type nodes
        obj = self._read_object_node(obj_type, obj_end)

        # check if all bytes were read correctly
        obj_size = r.tell() - obj_pos
        if obj_size != obj_info.byte_size:
            raise SerializationError("Wrong object size for path %d: %d != %d"
                                     % (path_id, obj_size, obj_info.byte_size))

        return obj

    def update_type_db(self, signature=None):
        types_added = 0

        # skip scan entirely if there are no embedded types
        if not self.types_raw:
            return types_added

        if not signature:
            signature = self.types.get("signature")

        self.type_db.signature = signature
        self.type_db.version = self.header.version
        self.type_db.order = self.r.order

        for class_id in self.types.classes:
            # ignore script types
            if class_id <= 0:
                continue

            # ignore types
            if class_id not in self.types_raw:
                continue

            class_type = self.types.classes[class_id]

            # create type files that don't exist yet
            if self.header.version > 13:
                if self.type_db.add(self.types_raw[class_id], class_id,
                                    class_type.old_type_hash):
                    types_added += 1
            else:
                if (class_type and signature and
                        self.type_db.add_old(self.types_raw[class_id], class_id)):
                    types_added += 1

        return types_added

    def close(self):
        self.r.close()

class SerializedFileError(Exception):
    pass

class SerializationError(Exception):
    pass
