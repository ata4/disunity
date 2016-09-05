import sys
import os
import glob
import pynity

from pprint import pprint

def main(argv):
    app = argv.pop(0)
    path = argv.pop(0)

    for globpath in glob.iglob(path, recursive=True):
        if os.path.isdir(globpath):
            continue

        with pynity.Archive(globpath) as archive:
            pprint(archive.header)
            pprint(archive.entries)
            pprint(archive.blocks_info)

if __name__ == "__main__":
    sys.exit(main(sys.argv))