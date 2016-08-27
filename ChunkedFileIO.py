import io

class ChunkedFileIO(io.BufferedIOBase):

    def __init__(self, paths):
        self.chunks = []
        self.index = 0
        pos = 0
        for path in paths:
            chunk = self.Chunk(path, pos)
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
        if not data:
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
        super(ChunkedFileIO, self).close()

    class Chunk():

        def __init__(self, path, pos):
            self.handle = open(path, "rb")
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