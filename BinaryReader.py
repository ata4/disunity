import struct
from uuid import UUID

class BinaryReader:

    def __init__(self, file):
        self.be = False
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
        b = self.read_byte()
        while b and b != 0:
            buf.append(b)
            b = self.read_byte()

        return buf.decode("ascii")

    def read_struct(self, format):
        size = struct.calcsize(format)
        data = self.file.read(size)
        return struct.unpack(format, data)

    def read_int(self, type):
        if self.be:
            type = ">" + type
        return self.read_struct(type)[0]

    def read_uuid(self):
        data = self.read(16)
        return UUID(bytes=data)

    def read_byte(self):
        b = self.file.read(1)
        return b[0] if b else None

    def read_int8(self):
        return self.read_int("b")

    def read_uint8(self):
        return self.read_int("B")

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