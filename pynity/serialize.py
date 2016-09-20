import io
import os
import json
import logging
import hashlib

from .io import AutoCloseable, BinaryReader, ChunkedFileIO, ByteOrder
from .utils import ObjectDict
from .typedb import TypeDatabase

METAFLAG_ALIGN = 0x4000

log = logging.getLogger("pynity.serialize")

class SerializedFile(AutoCloseable):

    versions = [5, 6, 8, 9, 14, 15]
    read_prim = {
        "bool":             BinaryReader.read_bool8,
        "SInt8":            BinaryReader.read_int8,
        "UInt8":            BinaryReader.read_uint8,
        "char":             BinaryReader.read_uint8,
        "SInt16":           BinaryReader.read_int16,
        "short":            BinaryReader.read_int16,
        "UInt16":           BinaryReader.read_uint16,
        "unsigned short":   BinaryReader.read_uint16,
        "SInt32":           BinaryReader.read_int32,
        "int":              BinaryReader.read_int32,
        "UInt32":           BinaryReader.read_uint32,
        "unsigned int":     BinaryReader.read_uint32,
        "SInt64":           BinaryReader.read_int64,
        "long":             BinaryReader.read_int64,
        "UInt64":           BinaryReader.read_uint64,
        "unsigned long":    BinaryReader.read_uint64,
        "float":            BinaryReader.read_float,
        "double":           BinaryReader.read_double,
    }

    types_cache = {}

    @classmethod
    def probe_path(cls, path):
        with ChunkedFileIO.open(path, "rb") as fp:
            return cls.probe_file(fp)

    @classmethod
    def probe_file(cls, file):
        r = BinaryReader(file, order=ByteOrder.BIG_ENDIAN)

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
        if not (cls.versions[0] <= header_version <= cls.versions[-1]):
            return False

        # check file size
        return file_size == header_file_size

    def __init__(self, file):
        self.string_mapper = StringMapper()
        self.type_db = TypeDatabase()

        # open file and make some basic checks to make sure this is actually a
        # serialized file
        if isinstance(file, str):
            fp = ChunkedFileIO.open(file, "rb")
        else:
            fp = file

        self.r = BinaryReader(fp, order=ByteOrder.BIG_ENDIAN)

        # read metadata
        self._read_header()
        self._read_types()
        self._read_object_info()
        self._read_script_types()
        self._read_externals()

    def _read_header(self):
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

    def _read_types(self):
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

        num_classes = r.read_int32()
        for _ in range(num_classes):
            class_type = ObjectDict()
            class_id = r.read_int32()

            if self.header.version > 13:
                if class_id < 0:
                    class_type.script_id = r.read_hash128()

                class_type.old_type_hash = r.read_hash128()

                if types.embedded:
                    class_type.type_tree = self._read_type_node()
                else:
                    class_type.type_tree = None
            else:
                type_pos = r.tell()
                class_type.type_tree = self._read_type_node_old()
                type_size = r.tell() - type_pos

                # create hash from binary type
                r.seek(type_pos)
                type_tree_raw = r.read(type_size)
                class_type.old_type_hash = hashlib.md5(type_tree_raw).hexdigest()

            if class_id in types.classes:
                raise SerializedFileError("Duplicate class ID %d" % class_id)

            types.classes[class_id] = class_type

        # padding
        if 6 < self.header.version < 13:
            r.read_int32()

    def _read_type_node(self):
        r = self.r

        # read sizes
        num_fields = r.read_int32()
        string_table_len = r.read_int32()

        # read local string table first so the strings can be assigned in one go
        tree_pos = r.tell()
        tree_len = 24 * num_fields
        r.seek(tree_len, io.SEEK_CUR)
        string_table_buf = r.read(string_table_len)
        string_table = self.string_mapper.get(string_table_buf)
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

    def _read_type_node_old(self):
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
            field.children.append(self._read_type_node_old())

        return field

    def _read_object_info(self):
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

    def _read_script_types(self):
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

    def _read_externals(self):
        r = self.r

        externals = self.externals = []

        num_entries = r.read_int32()
        for _ in range(num_entries):
            external = ObjectDict()

            if self.header.version > 5:
                external.asset_path = r.read_cstring()

            external.guid = r.read_uuid()
            external.type = r.read_int32()
            external.file_path = r.read_cstring()

            externals.append(external)

    def _read_object_node(self, obj_type):
        r = self.r

        if log.isEnabledFor(logging.DEBUG):
            log.debug("%d %s %s" % (r.tell(), obj_type.type, obj_type.name))

        if obj_type.is_array:
            # unpack "Array" objects to native Python arrays
            type_size = obj_type.children[0]
            type_data = obj_type.children[1]

            size = self._read_object_node(type_size)
            if type_data.type in ("SInt8", "UInt8", "char"):
                # read byte array
                obj = r.read(size)
            else:
                # read generic array
                obj = []
                for _ in range(size):
                    obj.append(self._read_object_node(type_data))

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
                obj[child.name] = self._read_object_node(child)

        if obj_type.type == "string":
            # convert string objects to native Python strings
            try:
                obj = obj.Array.decode("utf-8")
            except UnicodeDecodeError:
                # could be a TextAsset that contains binary data, return raw string
                log.warn("Can't decode string at %d as UTF-8, "
                         "using raw data instead" % r.tell())
                obj = obj.Array
        elif obj_type.type == "vector":
            # unpack collection containers
            obj = obj.Array

        return obj

    def read_object(self, path_id):
        # get object info
        object_info = self.objects.get(path_id)
        if not object_info:
            raise ValueError("Invalid path ID: %d" % path_id)

        # get object type class
        object_class = self.types.classes.get(object_info.type_id)

        # get object type from the embedded data, otherwise from database
        if self.header.version > 13:
            if self.types.embedded:
                object_type = object_class.type_tree
            else:
                object_type = self.type_db.get(object_info.type_id,
                                               object_class.old_type_hash)
        else:
            if object_class:
                object_type = object_class.type_tree
            else:
                object_type = self.type_db.get_old(object_info.type_id,
                                                   self.types.signature)

        # cancel if there's no type information available
        if not object_type:
            return

        # seek to object data start position
        object_pos = self.header.data_offset + object_info.byte_start
        self.r.seek(object_pos, io.SEEK_SET)

        # deserialize all type nodes
        object = self._read_object_node(object_type)

        # check if all bytes were read correctly
        object_size = self.r.tell() - object_pos
        if object_size != object_info.byte_size:
            raise SerializationError("Wrong object size for path %d: %d != %d"
                                     % (path_id, object_size, object_info.byte_size))

        return object

    def scan_types(self, signature=None):
        script_dir = os.path.dirname(__file__)
        types_dir = os.path.join(script_dir, "resources", "types")

        for class_id in self.types.classes:
            # ignore script types
            if class_id <= 0:
                continue

            class_type = self.types.classes[class_id]

            # create type files that don't exist yet
            if self.header.version > 13:
                if self.types.embedded:
                    self.type_db.add(class_type.type_tree, class_id,
                                     class_type.old_type_hash)
            else:
                if not signature:
                    signature = self.types.get("signature")

                if class_type and signature:
                    self.type_db.add_old(class_type.type_tree, class_id,
                                         class_type.old_type_hash,
                                         signature)

    def close(self):
        self.r.close()

class SerializedFileError(Exception):
    pass

class SerializationError(Exception):
    pass

class StringMapper:

    strings_global = {}

    def __init__(self):
        if self.strings_global:
            return

        script_dir = os.path.dirname(__file__)
        strings_path = os.path.join(script_dir, "resources", "strings.json")

        with open(strings_path) as fp:
            json_strings = json.load(fp)

        self.strings_global = self._create_map(json_strings, offset=1 << 31)

    def _create_map(self, strings, offset=0):
        string_map = {}
        p = 0
        for string in strings:
            if not string:
                continue

            string_map[p + offset] = string
            p += len(string) + 1
        return string_map

    def get(self, buf):
        strings = [string.decode("ascii") for string in buf.split(b'\0')]
        string_map = self.strings_global.copy()
        string_map.update(self._create_map(strings))
        return string_map