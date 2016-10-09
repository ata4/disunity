import io
import os
import struct
import binascii

from enum import IntEnum

# re-implementation of shutil.copyfileobj with separate length and buffer size
# parameters
def copyfileobj(fsrc, fdst, length, bufsize=8192):
    while length:
        read_len = min(bufsize, length)
        fdst.write(fsrc.read(read_len))
        length -= read_len

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
        super(ChunkedFileIO, self).__init__()

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
        chunk = self._chunk()
        chunk.seek(chunk.start)
        return chunk

    def _chunk_find(self, pos):
        chunk = None
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
        if size == -1 or size is None:
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
            #data = bytes(data)

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

class BinaryIO():

    def __init__(self, fp, order=ByteOrder.LITTLE_ENDIAN):
        self._order = None
        self._tag = None
        self._fp = fp

        self.order = order

    def __enter__(self, *args, **kwargs):
        return self

    def __exit__(self, *args, **kwargs):
        self._fp.__exit__(*args, **kwargs)

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
        byte = self.read_uint8()
        while byte:
            buf.append(byte)
            byte = self.read_uint8()

        if byte is None:
            raise IOError("Unexpected EOF while reading C string")

        return buf.decode("ascii")

    def read_struct(self, fmt, size=None):
        if size is None:
            size = struct.calcsize(fmt)
        data = self.read(size)
        if len(data) == size:
            return struct.unpack(fmt, data)

    def read_num(self, stype, size=None):
        data = self.read_struct(self._tag + stype, size)
        if data:
            return data[0]

    def read_hex(self, length):
        data = self.read(length)
        return binascii.hexlify(data).decode("ascii")

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

    def write_cstring(self, string):
        return self.write(string.encode("ascii")) + self.write(b"\0")

    def write_struct(self, fmt, *values):
        return self.write(struct.pack(fmt, *values))

    def write_num(self, stype, value):
        return self.write_struct(self._tag + stype, value)

    def write_hex(self, hexstr):
        data = binascii.unhexlify(hexstr.encode("ascii"))
        return self.write(data)

    def write_int8(self, value):
        return self.write_num("b", value)

    def write_uint8(self, value):
        return self.write_num("B", value)

    def write_int16(self, value):
        return self.write_num("h", value)

    def write_uint16(self, value):
        return self.write_num("H", value)

    def write_int32(self, value):
        return self.write_num("i", value)

    def write_uint32(self, value):
        return self.write_num("I", value)

    def write_int64(self, value):
        return self.write_num("q", value)

    def write_uint64(self, value):
        return self.write_num("Q", value)

    def write_float(self, value):
        return self.write_num("f", value)

    def write_double(self, value):
        return self.write_num("d", value)

    def write_bool8(self, value):
        return self.write_uint8(int(value))

    def write_bool16(self, value):
        return self.write_uint16(int(value))

    def write_bool32(self, value):
        return self.write_uint32(int(value))
