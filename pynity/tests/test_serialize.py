import unittest
import os
import logging

import pynity

class TestSerializeMeta(type):

    def __new__(mcs, name, bases, dict):
        files = [
            "v5_r2.0.1r2_typed.assets",
            "v6_r2.6.0f7_typed.assets",
            "v8_r3.1.0f4_typed.assets",
            "v9_r4.2.0f4_typed.assets",
            "v9_r4.5.4f1_typeless.assets.split0",
            "v14_r5.0.0f4_typed.assets",
            "v14_r5.0.0f4_typeless.assets",
            "v15_r5.2.3f1_typed.assets",
        ]

        path_base = os.path.dirname(__file__)
        path_resources = os.path.join(path_base, "resources", "serialized")

        for file in files:
            test_path = os.path.join(path_resources, file)
            dict["test_probe_" + file] = (
                lambda self, path=test_path: self._test_probe(path)
            )
            dict["test_deserialize_" + file] = (
                lambda self, path=test_path: self._test_deserialize(path)
            )

        return type.__new__(mcs, name, bases, dict)

class TestSerialize(unittest.TestCase, metaclass=TestSerializeMeta):

    @classmethod
    def setUpClass(cls):
        logging.getLogger().setLevel(logging.ERROR)

    def _test_probe(self, path):
        self.assertTrue(pynity.SerializedFile.probe_path(path))

    def _test_deserialize(self, file):
        with pynity.SerializedFile(file) as sf:
            for obj in sf.objects.values():
                self.assertIsNotNone(obj.instance)