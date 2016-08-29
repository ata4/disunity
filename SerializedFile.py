import io
import os

from pprint import pprint

from BinaryReader import *
from ObjectDict import *
from ChunkedFileIO import *
from AutoCloseable import *

VERSION_MIN = 5
VERSION_MAX = 15

class SerializedFile(AutoCloseable):

    versions_tested = [9, 14, 15]

    def __init__(self, path):
        self.stmapper = StringTableMapper()

        # open file and make some basic checks to make sure this is actually a serialized file
        self.r = self._create_reader(path)
        self.valid = self._validate(self.r)

        if not self.valid:
            return

        # read metadata
        self.header = self._read_header(self.r)
        self.types = self._read_types(self.r)
        self.objects = self._read_object_info(self.r)
        if self.header.version > 10:
            self.script_types = self._read_script_types(self.r)
        self.externals = self._read_externals(self.r)

    def _create_reader(self, path):
        fname, fext = os.path.splitext(path)

        if fext == ".split0":
            index = 0
            splitpath = fname + fext
            splitpaths = []
            while os.path.exists(splitpath):
                splitpaths.append(splitpath)
                index += 1
                splitpath = fname + ".split%d" % index

            return BinaryReader(ChunkedFileIO(splitpaths, "rb"))
        else:
            return BinaryReader(open(path, "rb"))

    def _validate(self, r):
        r.be = True

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

    def _read_header(self, r):
        # the header always uses big-endian byte order
        r.be = True

        header = ObjectDict()
        header.metadata_size = r.read_int32()
        header.file_size = r.read_int32()
        header.version = r.read_int32()
        header.data_offset = r.read_int32()

        if header.data_offset > header.file_size:
            raise RuntimeError("Invalid data_offset %d" % header.data_offset)

        if header.metadata_size > header.file_size:
            raise RuntimeError("Invalid metadata_size %d" % header.metadata_size)

        if header.version >= 9:
            header.endianness = r.read_int8()
            r.read(3) # reserved

        # newer formats use little-endian for the rest of the file
        if header.version > 5:
            r.be = False

        if not header.version in self.versions_tested:
            raise NotImplementedError("Unsupported format version %d" % header.version)

        return header

    def _read_types(self, r):
        types = ObjectDict()

        # older formats store the object data before the structure data
        if self.header.version < 9:
            types_offset = self.header.file_size - self.header.metadata_size + 1
            r.seek(types_offset)

        if self.header.version > 6:
            types.signature = r.read_cstring()
            types.attributes = r.read_int32()

        if self.header.version > 13:
            types.embedded = r.read_int8() != 0

        types.classes = {}

        num_classes = r.read_int32()
        for i in range(0, num_classes):
            bclass = ObjectDict()

            class_id = r.read_int32()

            if self.header.version > 13:
                if class_id < 0:
                    bclass.script_id = r.read_hash128()

                bclass.old_type_hash = r.read_hash128()

                if types.embedded:
                    bclass.type_tree = self._read_type_node(r)
                else:
                    bclass.type_tree = None
            else:
                bclass.type_tree = self._read_type_node_old(r)

            if class_id in types.classes:
                raise RuntimeError("Duplicate class ID %d" % class_id)

            types.classes[class_id] = bclass

        # padding
        if self.header.version > 6 and self.header.version < 13:
            r.read_int32()

        return types

    def _read_type_node(self, r):
        fields = []
        num_fields = r.read_int32()
        string_table_len = r.read_int32()

        # read field list
        for i in range(num_fields):
            field = ObjectDict()
            field.type = None
            field.name = None
            field.version = r.read_int16()
            field.tree_level = r.read_uint8()
            field.is_array = r.read_uint8() != 0
            field.type_offset = r.read_uint32()
            field.name_offset = r.read_uint32()
            field.size = r.read_int32()
            field.index = r.read_int32()
            field.meta_flag = r.read_int32()

            fields.append(field)

        # read local string table
        string_table_buf = r.read(string_table_len)
        string_table = self.stmapper.get(string_table_buf)

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
                for i in range(-tree_level_diff):
                    node_stack.pop()
                node_stack[-1].children.append(node)

            node_prev = node

        return node_root

    def _read_type_node_old(self, r):
        field = ObjectDict()
        field.type = r.read_cstring()
        field.name = r.read_cstring()
        field.size = r.read_int32()
        field.index = r.read_int32()
        field.is_array = r.read_int32() != 0
        field.version = r.read_int32()
        field.metaFlag = r.read_int32()
        field.children = []

        num_children = r.read_int32()
        for i in range(num_children):
            field.children.append(self._read_type_node_old(r))

        return field

    def _read_object_info(self, r):
        objects = {}

        num_entries = r.read_int32()

        for i in range(0, num_entries):
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
                obj.is_destroyed = r.read_int16() != 0

            if self.header.version > 14:
                obj.stripped = r.read_int8() != 0

            if path_id in objects:
                raise RuntimeError("Duplicate path ID %d" % path_id)

            objects[path_id] = obj

        return objects

    def _read_script_types(self, r):
        script_types = []

        num_entries = r.read_int32()

        for i in range(0, num_entries):
            r.align(4)

            script_type = ObjectDict()
            script_type.serialized_file_index = r.read_int32()
            script_type.identifier_in_file = r.read_int64()

            script_types.append(script_type)

        return script_types

    def _read_externals(self, r):
        externals = []

        num_entries = r.read_int32()
        for i in range(0, num_entries):
            external = ObjectDict()

            if self.header.version > 5:
                external.asset_path = r.read_cstring()

            external.guid = r.read_uuid()
            external.type = r.read_int32()
            external.file_path = r.read_cstring()

            externals.append(external)

        return externals

    def read_object(self, path_id):
        pass

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
