import logging
import tempfile
import unittest

from pynity.typedb import TypeDatabase, TypeException
from pynity.io import ByteOrder

class TestStringTable(unittest.TestCase):

    class_id = 1
    version = "4.6.1f1"
    hash = "0123456789abcdef"
    data = b"dummy"

    @classmethod
    def setUpClass(cls):
        logging.getLogger().setLevel(logging.ERROR)

    def setUp(self):
        self.tmpdir = tempfile.TemporaryDirectory()
        self.tdb = TypeDatabase(self.tmpdir.name)
        self.tdb.version = 15
        self.tdb.order = ByteOrder.LITTLE_ENDIAN
        self.tdb.signature = self.version

    def tearDown(self):
        self.tmpdir.cleanup()

    def test_add(self):
        self.assertRaises(TypeException, self.tdb.open, self.class_id, self.hash)
        self.assertTrue(self.tdb.add(self.data, self.class_id, self.hash))

        with self.tdb.open(self.class_id, self.hash) as fp:
            self.assertEqual(fp.read(), self.data)

    def test_add_old(self):
        self.assertRaises(TypeException, self.tdb.open_old, self.class_id, self.version)
        self.assertTrue(self.tdb.add_old(self.data, self.class_id))

        # direct match
        with self.tdb.open_old(self.class_id, self.version) as fp:
            self.assertEqual(fp.read(), self.data)

        # close match
        with self.tdb.open_old(self.class_id, "4.6.1f2") as fp:
            self.assertEqual(fp.read(), self.data)

        # vague match
        with self.tdb.open_old(self.class_id, "4.6.0f1") as fp:
            self.assertEqual(fp.read(), self.data)

        # no match
        self.assertRaises(TypeException, self.tdb.open_old, self.class_id, "4.5.0f1")
