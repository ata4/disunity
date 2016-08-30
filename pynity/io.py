import io
import os
import struct
import binascii

from uuid import UUID

from .utils import AutoCloseable

class UnityFile(io.BufferedIOBase):

    def __init__(self, path, mode):
        self.chunks = []
        self.index = 0

        if not os.path.exists(path):
            # add ".split0" if the file doesn't exist in the normal path
            if path[-7:] != ".split0":
                path_split = path + ".split0"
                if not os.path.exists(path_split):
                    raise FileNotFoundError(path)
                else:
                    path = path_split
            else:
                raise FileNotFoundError(path)

        fname, fext = os.path.splitext(path)
        paths = []

        # discover .splitXX parts
        if fext[:6] == ".split":
            index = 0
            splitpath = fname + ".split0"
            while os.path.exists(splitpath):
                paths.append(splitpath)
                index += 1
                splitpath = fname + ".split%d" % index
        else:
            paths.append(path)

        if not paths:
            raise FileNotFoundError(path)

        # open chunks
        pos = 0
        for path in paths:
            chunk = Chunk(path, mode, pos)
            # filter out empty chunks
            if chunk.size == 0:
                chunk.close()
                continue
            pos += chunk.size
            self.chunks.append(chunk)

    def chunk(self):
        return self.chunks[self.index]

    def chunk_next(self):
        if self.index + 1 >= len(self.chunks):
            return None

        self.index += 1
        return self.chunk()

    def chunk_find(self, pos):
        for self.index, chunk in enumerate(self.chunks):
            if chunk.end >= pos:
                break
        return chunk

    def read(self, size=-1):
        chunk = self.chunk()
        data = chunk.read(size)

        # get next chunk if required
        if size > 0 and not data:
            chunk = self.chunk_next()
            if chunk:
                data = chunk.read(size)

        return data

    def seek(self, offset, whence=io.SEEK_SET):
        chunk = self.chunk()

        # convert relative offset to absolute position
        pos = offset
        if whence == io.SEEK_CUR:
            pos += chunk.tell()
        elif whence == io.SEEK_END:
            pos += self.chunks[-1].end
        elif whence != io.SEEK_SET:
            raise NotImplementedError()

        # find new chunk if absolute position is outside current chunk
        if pos < chunk.start or pos > chunk.end:
            chunk = self.chunk_find(pos)

        chunk.seek(pos)

    def tell(self):
        return self.chunk().tell()

    def close(self):
        for chunk in self.chunks:
            chunk.close()
        super(UnityFile, self).close()

class Chunk():

    def __init__(self, path, mode, pos):
        self.handle = open(path, mode)
        self.handle.seek(0, io.SEEK_END)
        self.size = self.handle.tell()
        self.handle.seek(0, io.SEEK_SET)
        self.start = pos
        self.end = pos + self.size

    def tell(self):
        return self.start + self.handle.tell()

    def read(self, size=-1):
        return self.handle.read(size)

    def seek(self, offset):
        offset -= self.start
        if offset < 0 or offset > self.size:
            raise ValueError("Offset out of range:", offset)
        self.handle.seek(offset, io.SEEK_SET)

    def close(self):
        self.handle.close()
        
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