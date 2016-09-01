import lzma
import shutil

from enum import Enum
from pprint import pprint

from .io import *
from .utils import *

class Archive(AutoCloseable):

    versions = [1, 2, 3, 6]
    signatures = ["UnityWeb", "UnityRaw", "UnityFS"]

    def __init__(self, path, debug=False):
        self.debug = debug

        self.r = BinaryReader(open(path, "rb"), be=True)
        self.header = self._read_header(self.r)
        self._create_data_stream()
        self.entries = self._read_entries(self.rd)

    def _create_data_stream(self):
        if self.header.signature == "UnityWeb":
            self.fpd = lzma.open(self.r.fp, "rb")
            self.rd = BinaryReader(self.fpd, be=True)
        else:
            self.fpd = self.r.fp
            self.rd = self.r

    def _read_header(self, r):
        header = ObjectDict()

        header.signature = r.read_cstring()
        if not header.signature in self.signatures:
            raise RuntimeError("Invalid signature")

        header.version = r.read_int32()
        if not header.version in self.versions:
            raise NotImplementedError("Unsupported format version %d"
                                      % header.version)

        header.unity_version = r.read_cstring()
        header.unity_revision = r.read_cstring()

        if header.version > 3:
            # new UnityFS format
            header.size = r.read_uint64()
            header.compressed_blocks_info_size = r.read_uint32()
            header.uncompressed_blocks_info_size = r.read_uint32()
            header.flags = r.read_uint32()
        else:
            # old UnityWeb/UnityRaw format
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

            # padding
            r.read_uint8()

        return header

    def _read_entries(self, r):
        if self.header.version > 3:
            raise NotImplementedError("UnityFS read entries")
        else:
            # read StreamingInfo structs
            entries = []
            num_entries = r.read_uint32()
            for i in range(num_entries):
                entry = ObjectDict()
                entry.path = r.read_cstring()
                entry.offset = r.read_uint32()
                entry.size = r.read_uint32()
                entries.append(entry)

            return entries

    def extract(self, dir, entries=None):
        if self.header.version > 3:
            raise NotImplementedError("UnityFS extract")
        else:
            if not entries:
                entries = self.entries

            for entry in entries:
                print(entry.path)
                entry_path = os.path.join(dir, entry.path)
                os.makedirs(os.path.dirname(entry_path), exist_ok=True)
                fileobj_slice(self.fpd, entry_path, entry.offset, entry.size)

    def close(self):
        self.r.close()