import os

class StringTableReader:

    strings_global = None

    def __init__(self):
        if not self.strings_global:
            script_dir = os.path.dirname(__file__)
            strings_path = os.path.join(script_dir, "resources", "strings.bin")
            with open(strings_path, "rb") as file:
                self.strings_global = self.map(file.read(), 1 << 31)

    def get(self, buf):
        strings = self.strings_global.copy()
        strings.update(self.map(buf, 0))
        return strings

    def map(self, buf, base):
        strings = {}
        p = 0
        for i, c in enumerate(buf):
            if c == 0:
                strings[base + p] = buf[p:i].decode('ascii')
                p = i + 1
        return strings
