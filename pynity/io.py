import io
import os
import struct
import binascii

from enum import IntEnum

class AutoCloseable:

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()

    def close(self):
        pass

class ChunkedFileIO(io.BufferedIOBase):

    @classmethod
    def open(cls, path, mode):
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
            # open chunked files
            if "w" in mode:
                raise NotImplementedError("chunked write access")

            return cls(paths, mode)

    def __init__(self, paths, mode):
        self._chunks = []
        self._index = 0
        self._size = 0

        # open chunks
        for path in paths:
            chunk = self.Chunk(path, mode, self._size)
            # filter out empty chunks
            if chunk.size == 0:
                chunk.close()
                continue
            self._size += chunk.size
            self._chunks.append(chunk)

    def _chunk(self):
        return self._chunks[self._index]

    def _chunk_next(self):
        if self._index + 1 >= len(self._chunks):
            return None

        self._index += 1
        c = self._chunk()
        c.seek(c.start)
        return c

    def _chunk_find(self, pos):
        for self._index, chunk in enumerate(self._chunks):
            if chunk.end >= pos:
                break
        return chunk

    def readable(self):
        return True

    def writable(self):
        return False

    def seekable(self):
        return True

    def read(self, size=-1):
        if size == -1 or size == None:
            size = self._size - self.tell()

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
            pos += self._chunks[-1].end
        elif whence != io.SEEK_SET:
            raise NotImplementedError()

        # find new chunk if absolute position is outside current chunk
        if pos < chunk.start or pos > chunk.end:
            chunk = self._chunk_find(pos)

        chunk.seek(pos)

    def tell(self):
        return self._chunk().tell()

    def close(self):
        for chunk in self._chunks:
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
            if offset < 0:
                raise ValueError("Negative offset: %d" % offset)
            self.handle.seek(offset, io.SEEK_SET)

        def close(self):
            self.handle.close()

class ByteOrder(IntEnum):
    LITTLE_ENDIAN = 0
    BIG_ENDIAN = 1

class BinaryReader():

    def __init__(self, fp, order=ByteOrder.LITTLE_ENDIAN):
        self.order = order
        self._fp = fp

    def __getattr__(self, attr):
        return getattr(self._fp, attr)

    @property
    def order(self):
        return self._order

    @order.setter
    def order(self, value):
        self._order = value
        self._tag = ("<", ">")[int(value)]

    def align(self, pad):
        pos = self.tell()
        newpos = (pos + pad - 1) // pad * pad
        if newpos != pos:
            self.seek(newpos)

    def read_cstring(self):
        buf = bytearray()
        b = self.read_byte()
        while b:
            buf.append(b)
            b = self.read_byte()

        if b is None:
            raise IOError("Unexpected EOF while reading C string")

        return buf.decode("ascii")

    def read_struct(self, format, size=None):
        if size is None:
            size = struct.calcsize(format)
        data = self.read(size)
        return struct.unpack(format, data)

    def read_num(self, type, size=None):
        return self.read_struct(self._tag + type, size)[0]

    def read_hex(self, length):
        data = self.read(length)
        return binascii.hexlify(data).decode("ascii")

    def read_byte(self):
        b = self.read(1)
        if b == b"":
            return None
        else:
            return b[0]

    def read_int8(self):
        return self.read_num("b", 1)

    def read_uint8(self):
        return self.read_num("B", 1)

    def read_int16(self):
        return self.read_num("h", 2)

    def read_uint16(self):
        return self.read_num("H", 2)

    def read_int32(self):
        return self.read_num("i", 4)

    def read_uint32(self):
        return self.read_num("I", 4)

    def read_int64(self):
        return self.read_num("q", 8)

    def read_uint64(self):
        return self.read_num("Q", 8)

    def read_float(self):
        return self.read_num("f", 4)

    def read_double(self):
        return self.read_num("d", 8)

    def read_bool8(self):
        return bool(self.read_uint8())

    def read_bool16(self):
        return bool(self.read_uint16())

    def read_bool32(self):
        return bool(self.read_uint32())