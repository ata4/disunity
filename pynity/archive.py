import lzma
import lz4
import io
import os
import struct

from enum import Enum

from . import utils, ioutils

class ArchiveFile(ioutils.AutoCloseable):

    versions = [1, 2, 3, 6]
    signatures = ["UnityWeb", "UnityRaw", "UnityFS"]

    @classmethod
    def open(cls, path):
        r = ioutils.BinaryIO(open(path, "rb"), order=ioutils.BIG_ENDIAN)

        signature = r.read_cstring()
        if signature not in cls.signatures:
            raise ArchiveError("Invalid signature")

        version = r.read_int32()
        if version not in cls.versions:
            raise NotImplementedError("Unsupported format version %d"
                                      % header.version)

        # Note: Unity 5.3 uses "UnityWeb" signature for web builds, but the files
        # are actually in UnityFS format, so check the format version instead.
        if version > 3:
            return ArchiveFileFS(r, signature, version)
        else:
            return ArchiveFileWeb(r, signature, version)

    @classmethod
    def probe(cls, path):
        with open(path, "rb") as fp:
            if fp.read(5) != b"Unity":
                return False

            if fp.read(3) not in (b"Web", b"Raw", b"FS\0"):
                return False

        return True

    def __init__(self, r, signature, version):
        self.r = self.rd = r
        self.header = utils.ObjectDict()
        self.header.signature = signature
        self.header.version = version
        self.entries = []

        self._read_header()
        self._read_entries()

    def _read_header(self):
        r = self.r

        header = self.header
        header.unity_version = r.read_cstring()
        header.unity_revision = r.read_cstring()

    def _read_entries(self):
        pass

    def close(self):
        self.rd.close()
        self.r.close()

class ArchiveFileWeb(ArchiveFile):

    def _read_header(self):
        super()._read_header()

        r = self.r

        header = self.header
        header.min_streamed_bytes = r.read_uint32()
        header.header_size = r.read_uint32()
        header.num_levels_before_streaming = r.read_uint32()
        header.num_levels = r.read_uint32()

        header.scene_byte_end = []
        for _ in range(header.num_levels):
            byte_end = (r.read_uint32(), r.read_uint32())
            header.scene_byte_end.append(byte_end)

        if header.version > 1:
            header.complete_file_size = r.read_uint32()

        if header.version > 2:
            header.data_header_size = r.read_uint32()

        r.read_uint8() # padding

    def _read_entries(self):
        if self.compression_method == Compression.LZMA:
            self.rd = ioutils.BinaryIO(lzma.open(self.r, "rb"), order=ioutils.BIG_ENDIAN)

        # read StreamingInfo list
        entries = self.entries
        rd = self.rd
        num_entries = rd.read_uint32()
        for _ in range(num_entries):
            entry = Entry(rd)
            entry.path = rd.read_cstring()
            entry.offset = rd.read_uint32()
            entry.size = rd.read_uint32()
            entries.append(entry)

    @property
    def compression_method(self):
        # UnityWeb data stream needs to be opened as compressed LZMA stream
        if self.header.signature == "UnityWeb":
            return Compression.LZMA
        else:
            return Compression.NONE

class ArchiveFileFS(ArchiveFile):

    def _read_header(self):
        super()._read_header()

        r = self.r

        header = self.header
        header.file_size = r.read_uint64()
        header.compressed_blocks_info_size = r.read_uint32()
        header.uncompressed_blocks_info_size = r.read_uint32()
        header.flags = r.read_uint32()

    def _read_entries(self):
        r = self.r

        header = self.header
        method = self.compression_method
        blocks_info_size_c = header.compressed_blocks_info_size
        blocks_info_size_u = header.uncompressed_blocks_info_size

        if self.has_directory_info_end:
            r.seek(header.file_size - blocks_info_size_c)
        else:
            r.align(2)

        blocks_info_data = self._read_block(method, blocks_info_size_c, blocks_info_size_u)

        rb = ioutils.BinaryIO(io.BytesIO(blocks_info_data), order=ioutils.BIG_ENDIAN)

        # read ArchiveStorageHeader::BlocksInfo
        blocks_info = self.blocks_info = utils.ObjectDict()
        blocks_info.uncompressed_data_hash = rb.read_hex(16)
        storage_blocks = blocks_info.storage_blocks = []

        num_blocks = rb.read_int32()
        for _ in range(num_blocks):
            storage_block = StorageBlock()
            storage_block.uncompressed_size = rb.read_uint32()
            storage_block.compressed_size = rb.read_uint32()
            storage_block.flags = rb.read_uint16()
            storage_blocks.append(storage_block)

        # check if there's one large LZMA block
        compression_method = blocks_info.storage_blocks[0].compression_method
        if len(blocks_info.storage_blocks) == 1 and compression_method == Compression.LZMA:
            # in newer archive formats, the LZMA stream header no longer includes
            # the uncompressed size, since it's already part of the archive header,
            # so the props need to be read manually and supplied to a custom filter
            r.order = ioutils.LITTLE_ENDIAN
            prop = r.read_uint8()
            dict_size = r.read_uint32()

            fprop = utils.ObjectDict()
            fprop.id = lzma.FILTER_LZMA1
            fprop.dict_size = dict_size
            fprop.lc = prop % 9
            prop //= 9
            fprop.lp = prop % 5
            fprop.pb = prop // 5

            fp = lzma.open(self.r, "rb", format=lzma.FORMAT_RAW, filters=[fprop])
        else:
            # TODO: implement as stream instead of decompressing all blocks in
            # memory at once
            data = bytearray()
            for block in blocks_info.storage_blocks:
                data.extend(self._read_block(block.compression_method,
                                             block.compressed_size,
                                             block.uncompressed_size))

            fp = io.BytesIO(data)

        self.rd = ioutils.BinaryIO(fp, order=ioutils.BIG_ENDIAN)

        # read ArchiveStorageHeader::Node
        entries = self.entries = []
        num_entries = rb.read_int32()
        for _ in range(num_entries):
            entry = Entry(self.rd)
            entry.offset = rb.read_int64()
            entry.size = rb.read_int64()
            entry.flags = rb.read_uint32()
            entry.path = rb.read_cstring()
            entries.append(entry)

    def _read_block(self, method, compressed_size, uncompressed_size):
        block = self.r.read(compressed_size)

        if method == Compression.NONE:
            # nothing to do here
            return block
        elif method in (Compression.LZ4, Compression.LZ4HC):
            # lz4.decompress() wants the uncompressed size to be part of the input
            # data, not as a parameter, so we need to copy some bytes here...
            hdr = struct.pack("<I", uncompressed_size)
            return lz4.decompress(hdr + block)
        else:
            raise NotImplementedError("_read_block with " + method)

    @property
    def compression_method(self):
        return Compression(self.header.flags & 0x3f)

    @property
    def has_blocks_info(self):
        return self.header.flags & 0x40 != 0

    @property
    def has_directory_info_end(self):
        return self.header.flags & 0x80 != 0

class ArchiveError(Exception):
    pass

class Entry:

    def __init__(self, fp):
        self._fp = fp
        self.offset = 0
        self.size = 0
        self.flags = 0
        self.path = ""

    def read(self):
        self._fp.seek(self.offset)
        return self._fp.read(self.size)

    def extract(self, path):
        self._fp.seek(self.offset)
        with open(path, "wb") as fp:
            ioutils.copyfileobj(self._fp, fp, self.size)

class StorageBlock:

    def __init__(self):
        self.uncompressed_size = 0
        self.compressed_size = 0
        self.flags = 0

    @property
    def compression_method(self):
        return Compression(self.flags & 0x3f)

class Compression(Enum):

    NONE = 0
    LZMA = 1
    LZ4 = 2
    LZ4HC = 3
    LZHAM = 4
