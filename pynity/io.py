import io
import os
import struct
import binascii

from uuid import UUID

class AutoCloseable:

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()

    def close(self):
        pass

class ChunkedFileIO(io.BufferedIOBase):

    @staticmethod
    def open(path, mode):
        fname, fext = os.path.splitext(path)
        paths = []

        # discover .splitXX parts
        if fext == ".split0":
            index = 0
            splitpath = fname + fext
            while os.path.exists(splitpath):
                paths.append(splitpath)
                index += 1
                splitpath = fname + ".split%d" % index
        else:
            paths.append(path)

        if not paths:
            raise FileNotFoundError(path)
        elif len(paths) == 1:
            # open single files directly
            return open(paths[0], mode)
        else:
            # open chunked files with ChunkedFileIO
            return ChunkedFileIO(paths, mode)

    def __init__(self, paths, mode):
        self.chunks = []
        self.index = 0

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

    def _chunk(self):
        return self.chunks[self.index]

    def _chunk_next(self):
        if self.index + 1 >= len(self.chunks):
            return None

        self.index += 1
        c = self._chunk()
        c.seek(c.start)
        return c

    def _chunk_find(self, pos):
        for self.index, chunk in enumerate(self.chunks):
            if chunk.end >= pos:
                break
        return chunk

    def readable(self):
        return True

    def read(self, size=-1):
        if size == -1 or size == None:
            raise NotImplementedError("readall()")

        if size == 0:
            return bytes()

        chunk = self._chunk()
        data = chunk.read(size)

        # get next chunks if required
        if len(data) < size:
            # can't extend "byte" objects, so make current buffer mutable
            data = bytearray(data)

            # extend data while it's smaller than size
            while len(data) < size:
                size2 = size - len(data)
                data2 = chunk.read(size2)
                if not data2:
                    # no data, try next chunk
                    chunk = self._chunk_next()
                    if chunk:
                        # new chunk, continue reading
                        continue
                    else:
                        # no remaining chunks, cancel
                        break

                data.extend(data2)

            # make buffer immutable again
            data = bytes(data)

        return data

    def seek(self, offset, whence=io.SEEK_SET):
        chunk = self._chunk()

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
            chunk = self._chunk_find(pos)

        chunk.seek(pos)

    def tell(self):
        return self._chunk().tell()

    def close(self):
        for chunk in self.chunks:
            chunk.close()
        super(ChunkedFileIO, self).close()

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

    def __init__(self, fp, be=False):
        self.be = be
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
        tag = ("<", ">")[self.be]
        return self.read_struct(tag + type)[0]

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