import sys
import os

import disunity
import pynity

class ArchiveUnpack(disunity.CommandLineApp):

    def __init__(self):
        self.debug = False

    def process(self, path):
        path_out, _ = os.path.splitext(path)
        if path_out == path:
            path_out += "_"

        print(path)
        with pynity.Archive(path) as archive:
            if self.debug:
                pprint(archive.header)
                pprint(archive.entries)
                pprint(archive.blocks_info)
            else:
                for entry in archive.entries:
                    print(entry.path)
                    entry_path = os.path.join(path_out, entry.path)
                    os.makedirs(os.path.dirname(entry_path), exist_ok=True)
                    archive.extract(entry, entry_path)

if __name__ == "__main__":
    sys.exit(ArchiveUnpack().main(sys.argv))