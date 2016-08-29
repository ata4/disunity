import struct
import binascii
from uuid import UUID

from AutoCloseable import *

class BinaryReader(AutoCloseable):

    def __init__(self, fp):
        self.be = False
        self.fp = fp

    def close(self):
        self.fp.close()

    def tell(self):
        return self.fp.tell()

    def seek(self, offset, whence=0):
        self.fp.seek(offset, whence)

    def align(self, pad):
        pos = self.tell()
        newpos = (pos + pad - 1) // pad * pad
        if newpos != pos:
            self.seek(newpos)

    def read(self, size):
        return self.fp.read(size)

    def read_cstring(self):
        buf = bytearray()
        b = self.read_byte()
        while b and b != 0:
            buf.append(b)
            b = self.read_byte()

        return buf.decode("ascii")

    def read_struct(self, format):
        size = struct.calcsize(format)
        data = self.fp.read(size)
        return struct.unpack(format, data)

    def read_num(self, type):
        if self.be:
            type = ">" + type
        return self.read_struct(type)[0]

    def read_uuid(self):
        data = self.read(16)
        return UUID(bytes=data)

    def read_hash128(self):
        data = self.read(16)
        return binascii.hexlify(data).decode("ascii")

    def read_byte(self):
        b = self.fp.read(1)
        return b[0] if b else None

    def read_int8(self):
        return self.read_num("b")

    def read_uint8(self):
        return self.read_num("B")

    def read_int16(self):
        return self.read_num("h")

    def read_uint16(self):
        return self.read_num("H")

    def read_int32(self):
        return self.read_num("i")

    def read_uint32(self):
        return self.read_num("I")

    def read_int64(self):
        return self.read_num("q")

    def read_uint64(self):
        return self.read_num("Q")

    def read_float(self):
        return self.read_num("f")

    def read_double(self):
        return self.read_num("d")

    def read_bool8(self):
        return self.read_uint8() != 0

    def read_bool16(self):
        return self.read_uint16() != 0

    def read_bool32(self):
        return self.read_uint32() != 0