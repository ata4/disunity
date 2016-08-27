from BinaryReader import *
from munch import Munch

class SerializedFileReader:

    def read(self, file):
        r = BinaryReader(file)
        sf = Munch()
        self.read_header(r, sf)
        self.read_types(r, sf)
        self.read_objects(r, sf)
        if sf.header.version > 10:
            self.read_script_types(r, sf)
        self.read_externals(r, sf)

        return sf

    def read_header(self, r, sf):
        # the header always uses big-endian byte order
        r.be = True

        sf.header = Munch()
        sf.header.metadataSize = r.read_int32()
        sf.header.fileSize = r.read_int32()
        sf.header.version = r.read_int32()
        sf.header.dataOffset = r.read_int32()

        if sf.header.dataOffset > sf.header.fileSize:
            raise RuntimeError("Invalid dataOffset %d" % sf.header.dataOffset)

        if sf.header.metadataSize > sf.header.fileSize:
            raise RuntimeError("Invalid metadataSize %d" % sf.header.metadataSize)

        if sf.header.version >= 9:
            sf.header.endianness = r.read_int8()
            r.read(3) # reserved

        # newer formats use little-endian for the rest of the file
        if sf.header.version > 5:
            r.be = False

        # TODO: test more formats
        if sf.header.version != 15:
            raise NotImplementedError("Unsupported format version %d" % sf.header.version)

    def read_types(self, r, sf):
        sf.types = Munch()

        # older formats store the object data before the structure data
        if sf.header.version < 9:
            types_offset = sf.header.fileSize - sf.header.metadataSize + 1
            r.seek(types_offset)

        if sf.header.version > 6:
            sf.types.signature = r.read_cstring()
            sf.types.attributes = r.read_int32()

        if sf.header.version > 13:
            sf.types.embedded = r.read_int8() != 0

        sf.types.classes = {}

        num_classes = r.read_int32()
        for i in range(0, num_classes):
            bclass = Munch()

            class_id = r.read_int32()
            if class_id < 0:
                bclass.script_id = r.read_uuid()

            bclass.old_type_hash = r.read_uuid()

            if sf.types.embedded:
                # TODO
                raise NotImplementedError("Runtime type node reading")

            if class_id in sf.types.classes:
                raise RuntimeError("Duplicate class ID %d" % path_id)

            sf.types.classes[class_id] = bclass

    def read_objects(self, r, sf):
        sf.objects = {}

        num_entries = r.read_int32()

        for i in range(0, num_entries):
            if sf.header.version > 13:
                r.align(4)

            path_id = r.read_int64()

            obj = Munch()
            obj.byte_start = r.read_uint32()
            obj.byte_size = r.read_uint32()
            obj.type_id = r.read_int32()
            obj.class_id = r.read_int16()

            if sf.header.version > 13:
                obj.script_type_index = r.read_int16()
            else:
                obj.is_destroyed = r.read_int16() != 0

            if sf.header.version > 14:
                obj.stripped = r.read_int8() != 0

            if path_id in sf.objects:
                raise RuntimeError("Duplicate path ID %d" % path_id)

            sf.objects[path_id] = obj

    def read_script_types(self, r, sf):
        sf.script_types = []

        num_entries = r.read_int32()

        for i in range(0, num_entries):
            r.align(4)

            script_type = Munch()
            script_type.serialized_file_index = r.read_int32()
            script_type.identifier_in_file = r.read_int64()

            sf.script_types.append(script_type)

    def read_externals(self, r, sf):
        sf.externals = []

        num_entries = r.read_int32()
        for i in range(0, num_entries):
            external = Munch()

            if sf.header.version > 5:
                external.asset_path = r.read_cstring()

            external.guid = r.read_uuid()
            external.type = r.read_int32()
            external.file_path = r.read_cstring()

            sf.externals.append(external)