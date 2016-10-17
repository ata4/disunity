import unittest

import pynity.stringtable as stringtable

class TestStringTable(unittest.TestCase):

    strings_buf = b"ChannelInfo\0m_Center\0e00\0"
    strings_dict = {0: 'ChannelInfo', 12: 'm_Center', 21: 'e00'}

    def test_map(self):
        strings = stringtable.strmap(self.strings_buf)
        self.assertDictEqual(strings, self.strings_dict)

    def test_load(self):
        strings = stringtable.load(self.strings_buf)
        self.assertTrue(set(self.strings_dict.items()).issubset(set(strings.items())))
        self.assertEqual(strings[1<<31], "AABB")
