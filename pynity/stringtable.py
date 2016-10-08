import os

class StringTable:

    strings_global = {}

    @classmethod
    def map(cls, buf, offset=0):
        string_list = [string.decode("ascii") for string in buf.split(b'\0')]
        string_map = {}
        p = 0
        for string in string_list:
            if not string:
                continue

            string_map[p + offset] = string
            p += len(string) + 1
        return string_map

    @classmethod
    def load(cls, buf):
        if not cls.strings_global:
            script_dir = os.path.dirname(__file__)
            strings_path = os.path.join(script_dir, "resources", "types", "common.unitystrings")

            with open(strings_path, "rb") as fp:
                cls.strings_global = cls.map(fp.read(), offset=1<<31)

        strings = cls.strings_global.copy()
        strings.update(cls.map(buf))
        return strings
