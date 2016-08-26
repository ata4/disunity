import sys
import os
import struct
import glob
from pprint import pprint
from munch import Munch

class BinaryReader:

    be = False

    def __init__(self, file):
        self.file = file

    def tell(self):
        return self.file.tell()

    def seek(self, offset, whence=0):
        self.file.seek(offset, whence)

    def align(self, pad):
        pos = self.tell()
        newpos = (pos + pad - 1) // pad * pad
        if newpos != pos:
            self.seek(newpos)

    def read(self, size):
        return self.file.read(size)

    def read_cstring(self):
        buf = bytearray()
        b = self.read_int8()
        while b and b != 0:
            buf.append(b)
            b = self.read_int8()

        return buf.decode("ascii")

    def read_struct(self, format):
        size = struct.calcsize(format)
        data = self.file.read(size)
        return struct.unpack(format, data)

    def read_int(self, type):
        if self.be:
            type = ">" + type
        return self.read_struct(type)[0]

    def read_int8(self):
        b = self.file.read(1)
        return b[0] if b else None

    def read_int16(self):
        return self.read_int("h")

    def read_uint16(self):
        return self.read_int("H")

    def read_int32(self):
        return self.read_int("i")

    def read_uint32(self):
        return self.read_int("I")

    def read_int64(self):
        return self.read_int("q")

    def read_uint64(self):
        return self.read_int("Q")

class SerializedFileReader:

    def read(self, file):
        r = BinaryReader(file)
        sf = Munch()
        self.read_header(r, sf)
        self.read_types(r, sf)
        self.read_object_info(r, sf)
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
                bclass.script_id = r.read(16)

            bclass.old_type_hash = r.read(16)

            if sf.types.embedded:
                # TODO
                raise NotImplementedError("Runtime type node reading")

            if class_id in sf.types.classes:
                raise RuntimeError("Duplicate class ID %d" % path_id)

            sf.types.classes[class_id] = bclass

    def read_object_info(self, r, sf):
        sf.objects = {}

        num_entries = r.read_int32()

        if sf.header.version > 13:
            r.align(4)

        for i in range(0, num_entries):
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
                obj.stripped = r.read_int32() != 0

            if path_id in sf.objects:
                raise RuntimeError("Duplicate path ID %d" % path_id)

            sf.objects[path_id] = obj

def main(argv):
    app = argv.pop(0)
    path = argv.pop(0)

    reader = SerializedFileReader()

    for globpath in glob.iglob(path):
        _, fext = os.path.splitext(globpath)
        if fext == ".resource" or fext[0:6] == ".split":
            continue

        if os.path.isdir(globpath):
            continue

        print(globpath)
        with open(globpath, "rb") as file:
            sf = reader.read(file)
            #pprint(sf)

    return 0

if __name__ == "__main__":
    sys.exit(main(sys.argv))