import os
import io
import unittest

import pynity.ioutils as ioutils

class ChunkedFileIOTest(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        path_base = os.path.dirname(__file__)
        path_resources = os.path.join(path_base, "resources", "chunk")

        cls.path_file = os.path.join(path_resources, "chunk.split0")
        cls.file_data = bytearray()

        for i in range(3):
            path = os.path.join(path_resources, "chunk.split%d" % i)
            with open(path, "rb") as fp:
                cls.file_data.extend(fp.read())

    def setUp(self):
        self.file = ioutils.ChunkedFileIO.open(self.path_file, "rb")

    def tearDown(self):
        self.file.close()

    def test_attributes(self):
        self.assertTrue(self.file.readable())
        self.assertFalse(self.file.writable())
        self.assertTrue(self.file.seekable())

    def test_size(self):
        self.file.seek(0, io.SEEK_END)
        result = self.file.tell()
        self.assertEqual(result, len(self.file_data))

    def test_seek_absolute(self):
        self.assertEqual(self.file.tell(), 0)
        self.file.seek(512)
        self.assertEqual(self.file.tell(), 512)
        self.file.seek(1023, io.SEEK_SET)
        self.assertEqual(self.file.tell(), 1023)

    def test_seek_cur(self):
        self.file.seek(1023, io.SEEK_SET)
        self.file.seek(1, io.SEEK_CUR)
        self.assertEqual(self.file.tell(), 1024)
        self.file.seek(-3, io.SEEK_CUR)
        self.assertEqual(self.file.tell(), 1021)

    def test_seek_end(self):
        self.file.seek(0, io.SEEK_END)
        self.assertEqual(self.file.tell(), 2390)
        self.file.seek(-10, io.SEEK_END)
        self.assertEqual(self.file.tell(), 2380)

    def test_seek_outside(self):
        self.file.seek(50000, io.SEEK_SET)
        self.assertEqual(self.file.tell(), 50000)

    def test_seek_invalid(self):
        self.assertRaises(ValueError, self.file.seek, -100, io.SEEK_SET)
        self.file.seek(100, io.SEEK_SET)
        self.assertRaises(ValueError, self.file.seek, -101, io.SEEK_CUR)

    def test_read_zero(self):
        result = self.file.read(0)
        self.assertEqual(result, b"")

    def test_read_end_single_chunk_0(self):
        self.file.seek(1023)
        result = self.file.read(1)
        self.assertEqual(result, self.file_data[1023:1024])

    def test_read_end_single_chunk_1(self):
        self.file.seek(2047)
        result = self.file.read(1)
        self.assertEqual(result, self.file_data[2047:2048])

    def test_read_end_chunk_2(self):
        self.file.seek(-1, io.SEEK_END)
        result = self.file.read(1)
        self.assertEqual(result, self.file_data[-1:])

    def test_read_border_chunk_0_1(self):
        self.file.seek(1023)
        result = self.file.read(2)
        self.assertEqual(result, bytes([0x0C, 0xBD]))

    def test_read_border_chunk_1_2(self):
        self.file.seek(2047)
        result = self.file.read(2)
        self.assertEqual(result, bytes([0xFA, 0x46]))

    def test_read_border_chunk_0_1_2(self):
        self.file.seek(512)
        result = self.file.read(1707)
        self.assertEqual(result, self.file_data[512:2219])

    def test_read_all(self):
        result = self.file.read()
        self.assertEqual(result, self.file_data)

class BinaryIOTest(unittest.TestCase):

    def test_read_cstring(self):
        string = "Just a string"
        data = string.encode("ascii") + b"\0"
        r = ioutils.BinaryIO(io.BytesIO(data))

        self.assertEqual(r.read_cstring(), string)

    def test_read_cstring_eof(self):
        string = "Just a string"
        data = string.encode("ascii")
        r = ioutils.BinaryIO(io.BytesIO(data))

        self.assertRaises(IOError, r.read_cstring)

    def test_read_uint8(self):
        data = b"\x18"
        r = ioutils.BinaryIO(io.BytesIO(data))

        self.assertEqual(r.read_uint8(), 0x18)

    def test_read_uint8_eof(self):
        data = b""
        r = ioutils.BinaryIO(io.BytesIO(data))

        self.assertEqual(r.read_uint8(), None)

    def test_align(self):
        data = b"\0" * 64
        r = ioutils.BinaryIO(io.BytesIO(data))

        r.align(4)
        self.assertEqual(r.tell(), 0)

        r.seek(64)
        r.align(8)
        self.assertEqual(r.tell(), 64)

        r.seek(30)
        r.align(8)
        self.assertEqual(r.tell(), 32)

        r.seek(2)
        r.align(32)
        self.assertEqual(r.tell(), 32)

    def test_byteorder(self):
        r = ioutils.BinaryIO(io.BytesIO(b"\x12\x34"))

        self.assertEqual(r.order, ioutils.LITTLE_ENDIAN)
        self.assertEqual(r.read_int16(), 13330)

        r.seek(0)
        r.order = ioutils.BIG_ENDIAN
        self.assertEqual(r.order, ioutils.BIG_ENDIAN)
        self.assertEqual(r.read_int16(), 4660)