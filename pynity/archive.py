import lzma
import lz4
import io
import os
import struct

from enum import Enum

from .io import AutoCloseable, BinaryReader, ByteOrder
from .utils import ObjectDict

class Compression(Enum):
    NONE = 0
    LZMA = 1
    LZ4 = 2
    LZ4HC = 3
    LZHAM = 4

class Archive(AutoCloseable):

    versions = [1, 2, 3, 6]
    signatures = ["UnityWeb", "UnityRaw", "UnityFS"]

    def __init__(self, path):
        self.r = self.rd = BinaryReader(open(path, "rb"), order=ByteOrder.BIG_ENDIAN)
        self._read_header()

    def _read_header(self):
        r = self.r

        header = self.header = ObjectDict()

        header.signature = r.read_cstring()
        if not header.signature in self.signatures:
            raise RuntimeError("Invalid signature")

        header.version = r.read_int32()
        if not header.version in self.versions:
            raise NotImplementedError("Unsupported format version %d"
                                      % header.version)

        header.unity_version = r.read_cstring()
        header.unity_revision = r.read_cstring()

        # read more header information based on format version
        if header.version > 3:
            self._read_header_fs()
        else:
            self._read_header_web()

    def _read_header_web(self):
        r = self.r

        header = self.header
        header.min_streamed_bytes = r.read_uint32()
        header.header_size = r.read_uint32()
        header.num_levels_before_streaming = r.read_uint32()
        header.num_levels = r.read_uint32()

        header.scene_byte_end = []
        for i in range(header.num_levels):
            byte_end = (r.read_uint32(), r.read_uint32())
            header.scene_byte_end.append(byte_end)

        if header.version > 1:
            header.complete_file_size = r.read_uint32()

        if header.version > 2:
            header.data_header_size = r.read_uint32()

        r.read_uint8() # padding

        # open data stream
        if self.header.signature == "UnityWeb":
            rd = self.rd = BinaryReader(lzma.open(r, "rb"), order=ByteOrder.BIG_ENDIAN)

        # read StreamingInfo structs
        entries = self.entries = []
        num_entries = rd.read_uint32()
        for i in range(num_entries):
            entry = ObjectDict()
            entry.path = rd.read_cstring()
            entry.offset = rd.read_uint32()
            entry.size = rd.read_uint32()
            entries.append(entry)

        # unused
        self.blocks_info = None

    def _read_header_fs(self):
        r = self.r

        header = self.header
        header.file_size = r.read_uint64()
        header.compressed_blocks_info_size = r.read_uint32()
        header.uncompressed_blocks_info_size = r.read_uint32()

        flags = header.flags = ObjectDict()
        flags.raw = r.read_uint32()
        flags.compression_method = Compression(flags.raw & 0x3f)
        flags.blocks_info = flags.raw & 0x40 != 0
        flags.directory_info_end = flags.raw & 0x80 != 0

        # read entries
        method = flags.compression_method
        blocks_info_size_c = header.compressed_blocks_info_size
        blocks_info_size_u = header.uncompressed_blocks_info_size

        if header.flags.directory_info_end:
            r.seek(header.file_size - blocks_info_size_c)
        else:
            r.align(2)

        blocks_info_data = self._read_block(method, blocks_info_size_c, blocks_info_size_u)

        rb = BinaryReader(io.BytesIO(blocks_info_data), order=ByteOrder.BIG_ENDIAN)

        # read ArchiveStorageHeader::BlocksInfo
        self.blocks_info = blocks_info = ObjectDict()
        blocks_info.uncompressed_data_hash = rb.read_hash128()
        blocks_info.storage_blocks = []

        num_blocks = rb.read_int32()
        for _ in range(num_blocks):
            storage_block = ObjectDict()
            storage_block.uncompressed_size = rb.read_uint32()
            storage_block.compressed_size = rb.read_uint32()

            flags = storage_block.flags = ObjectDict()
            flags.raw = rb.read_uint16()
            flags.compression_method = Compression(flags.raw & 0x3f)

            blocks_info.storage_blocks.append(storage_block)

        # read ArchiveStorageHeader::Node strucs
        self.entries = entries = []
        num_entries = rb.read_int32()
        for _ in range(num_entries):
            entry = ObjectDict()
            entry.offset = rb.read_int64()
            entry.size = rb.read_int64()
            entry.flags = rb.read_uint32()
            entry.path = rb.read_cstring()
            entries.append(entry)

        # check if there's one large LZMA block
        compression_method = blocks_info.storage_blocks[0].flags.compression_method
        if len(blocks_info.storage_blocks) == 1 and compression_method == Compression.LZMA:
            # in newer archive formats, the LZMA stream header no longer includes
            # the uncompressed size, since it's already part of the archive header,
            # so the props need to be read manually and supplied to a custom filter
            r.order = ByteOrder.LITTLE_ENDIAN
            prop = r.read_uint8()
            dict_size = r.read_uint32()

            filter = ObjectDict()
            filter.id = lzma.FILTER_LZMA1
            filter.dict_size = dict_size
            filter.lc = prop % 9
            prop //= 9
            filter.lp = prop % 5
            filter.pb = prop // 5

            fp = lzma.open(self.r, "rb", format=lzma.FORMAT_RAW, filters=[filter])
        else:
            # TODO: implement as stream instead of decompressing all blocks in
            # memory at once
            data = bytearray()
            for block in blocks_info.storage_blocks:
                data.extend(self._read_block(block.flags.compression_method,
                    block.compressed_size, block.uncompressed_size))

            fp = io.BytesIO(data)

        self.rd = BinaryReader(fp, order=ByteOrder.BIG_ENDIAN)

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

    def extract(self, dir, *entries):
        if not entries:
            entries = self.entries

        for entry in entries:
            entry_path = os.path.join(dir, entry.path)
            os.makedirs(os.path.dirname(entry_path), exist_ok=True)

            self.rd.seek(entry.offset)

            with open(entry_path, "wb") as fp:
                length = entry.size
                while length:
                    data_len = min(4096, length)
                    data = self.rd.read(data_len)
                    fp.write(data)
                    length -= data_len

    def close(self):
        self.rd.close()
        self.r.close()