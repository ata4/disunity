import io
import os
import json

from .io import *
from .utils import *
from .log import *

VERSION_MIN = 5
VERSION_MAX = 15

METAFLAG_ALIGN = 0x4000

class SerializedFile(AutoCloseable):

    versions_tested = [5, 6, 8, 9, 14, 15]
    read_prim = {
        "bool": BinaryReader.read_bool8,
        "SInt8": BinaryReader.read_int8,
        "UInt8": BinaryReader.read_uint8,
        "char": BinaryReader.read_uint8,
        "SInt16": BinaryReader.read_int16,
        "short": BinaryReader.read_int16,
        "UInt16": BinaryReader.read_int16,
        "unsigned short": BinaryReader.read_int16,
        "SInt32": BinaryReader.read_int32,
        "int": BinaryReader.read_int32,
        "UInt32": BinaryReader.read_uint32,
        "unsigned int": BinaryReader.read_uint32,
        "SInt64": BinaryReader.read_int64,
        "long": BinaryReader.read_int64,
        "UInt64": BinaryReader.read_uint64,
        "unsigned long": BinaryReader.read_uint64,
        "float": BinaryReader.read_float,
        "double": BinaryReader.read_double,
    }

    def __init__(self, path):
        self.string_mapper = StringTableMapper()
        self.types_db = {}

        # open file and make some basic checks to make sure this is actually a serialized file
        self.r = BinaryReader(ChunkedFileIO.open(path, "rb"), be=True)
        self.valid = self._validate()

        if not self.valid:
            return

        # read metadata
        self._read_header()
        self._read_types()
        self._read_object_info()
        self._read_script_types()
        self._read_externals()

    def _validate(self):
        r = self.r

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
        if header_version < VERSION_MIN:
            return False

        if header_version > VERSION_MAX:
            return False

        # check file size
        return file_size == header_file_size

    def _read_header(self):
        r = self.r

        header = self.header = ObjectDict()
        header.metadata_size = r.read_int32()
        header.file_size = r.read_int32()
        header.version = r.read_int32()
        header.data_offset = r.read_int32()

        if header.data_offset > header.file_size:
            raise RuntimeError("Invalid data_offset %d" % header.data_offset)

        if header.metadata_size > header.file_size:
            raise RuntimeError("Invalid metadata_size %d" % header.metadata_size)

        if header.version > 8:
            header.endianness = r.read_int8()
            r.read(3) # reserved

        # newer formats use little-endian for the rest of the file
        if header.version > 5:
            r.be = False

        if not header.version in self.versions_tested:
            raise NotImplementedError("Unsupported format version %d"
                                      % header.version)

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
                class_type.type_tree = self._read_type_node_old()

            if class_id in types.classes:
                raise RuntimeError("Duplicate class ID %d" % class_id)

            types.classes[class_id] = class_type

        # padding
        if self.header.version > 6 and self.header.version < 13:
            r.read_int32()

    def _read_type_node_db(self, hash, class_id):
        # load from cache if possible
        if hash in self.types_db:
            return self.types_db[hash]

        path_script_dir = os.path.dirname(__file__)
        path_type_dir = os.path.join(path_script_dir, "resources", "types", str(class_id))
        path_type = os.path.join(path_type_dir, hash + ".json")

        if not os.path.exists(path_type):
            Log.warning("Type %s not found in file or database" % hash)
            self.types_db[hash] = None
            return

        Log.trace("Type %s loaded from database" % hash)

        with open(path_type) as file:
            type_tree = ObjectDict.from_dict(json.load(file))
            self.types_db[hash] = type_tree
            return type_tree

    def _read_type_node(self):
        r = self.r

        fields = []
        num_fields = r.read_int32()
        string_table_len = r.read_int32()

        # read field list
        for _ in range(num_fields):
            field = ObjectDict()
            field.type = None
            field.name = None
            field.version = r.read_int16()
            field.tree_level = r.read_uint8()
            field.is_array = r.read_bool8()
            field.type_offset = r.read_uint32()
            field.name_offset = r.read_uint32()
            field.size = r.read_int32()
            field.index = r.read_int32()
            field.meta_flag = r.read_int32()

            fields.append(field)

        # read local string table
        string_table_buf = r.read(string_table_len)
        string_table = self.string_mapper.get(string_table_buf)

        # convert list to tree structure
        node_stack = []
        node_prev = None
        node_root = None
        tree_level_prev = 0

        for field in fields:
            # assign strings
            field.name = string_table[field.name_offset]
            field.type = string_table[field.type_offset]

            # don't need those offsets anymore
            del field.name_offset
            del field.type_offset

            # convert to node
            node = field
            node.children = []

            # set root node
            if not node_root:
                node_root = node_prev = node
                node_stack.append(node)
                tree_level_prev = field.tree_level
                del field.tree_level
                continue

            # get tree level difference and move node up or down if required
            tree_level_diff = field.tree_level - tree_level_prev
            tree_level_prev = field.tree_level

            # don't need the tree level now either
            del field.tree_level

            if tree_level_diff > 0:
                node_prev.children.append(node)
                node_stack.append(node_prev)
            else:
                for _ in range(-tree_level_diff):
                    node_stack.pop()
                node_stack[-1].children.append(node)

            node_prev = node

        return node_root

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

            if self.header.version > 13:
                obj.script_type_index = r.read_int16()
            else:
                obj.is_destroyed = r.read_bool16()

            if self.header.version > 14:
                obj.stripped = r.read_bool8()

            if path_id in objects:
                raise RuntimeError("Duplicate path ID %d" % path_id)

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

        Log.trace(r.tell(), obj_type.type, obj_type.name)

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
            if not obj_type.type in self.read_prim:
                raise RuntimeError("Unknown primitive type %s" % obj_type.type)
            obj = self.read_prim[obj_type.type](r)

            # align if flagged
            if obj_type.meta_flag & METAFLAG_ALIGN != 0:
                r.align(4)
        else:
            # complex object with children
            obj_class = type(obj_type.type, (ObjectDict,), {})
            obj = obj_class()
            for child in obj_type.children:
                obj[child.name] = self._read_object_node(child)

        if obj_type.type == "string":
            # convert string objects to native Python strings
            obj = obj.Array.decode("utf-8")
        elif obj_type.type == "vector":
            # unpack collection containers
            obj = obj.Array

        return obj

    def read_object(self, path_id):
        object_info = self.objects.get(path_id)
        if not object_info:
            raise ValueError("Invalid path ID " + path_id)

        object_class = self.types.classes.get(object_info.type_id)
        if not object_class:
            return
        
        object_pos = self.header.data_offset + object_info.byte_start
        self.r.seek(object_pos, io.SEEK_SET)

        object_type = object_class.type_tree
        if not object_type:
            object_type = self._read_type_node_db(object_class.old_type_hash, object_info.type_id)

        if not object_type:
            return

        object = self._read_object_node(object_type)

        # check if all bytes were read correctly
        object_size = self.r.tell() - object_pos
        if object_size != object_info.byte_size:
            raise RuntimeError("Wrong object size for path %d: %d != %d"
                               % (path_id, object_size, object_info.byte_size))

        return object

    def scan_types(self):
        script_dir = os.path.dirname(__file__)
        types_dir = os.path.join(script_dir, "resources", "types")

        for class_id in self.types.classes:
            # ignore script types
            if class_id <= 0:
                continue

            class_type = self.types.classes[class_id]

            if "old_type_hash" in class_type and self.types.embedded and class_type.type_tree:
                # create type files that don't exist yet
                path_dir = os.path.join(types_dir, str(class_id))
                if not os.path.exists(path_dir):
                    os.makedirs(path_dir)

                path_type = os.path.join(path_dir, class_type.old_type_hash + ".json")
                if not os.path.exists(path_type):
                    Log.info("Added type " + class_type.old_type_hash)
                    with open(path_type, "w") as file:
                        json.dump(class_type.type_tree, file, indent=2, separators=(',', ': '))


    def close(self):
        self.r.close()

class StringTableMapper:

    strings_global = None

    def __init__(self):
        if not self.strings_global:
            script_dir = os.path.dirname(__file__)
            strings_path = os.path.join(script_dir, "resources", "strings.bin")
            with open(strings_path, "rb") as file:
                self.strings_global = self.map(file.read(), 1 << 31)

    def get(self, buf):
        strings = self.strings_global.copy()
        strings.update(self.map(buf, 0))
        return strings

    def map(self, buf, base):
        strings = {}
        p = 0
        for i, c in enumerate(buf):
            if c == 0:
                strings[base + p] = buf[p:i].decode('ascii')
                p = i + 1
        return strings
