import sys
import os
import glob
import json
from pprint import pprint

import pynity

def main(argv):
    app = argv.pop(0)
    path = argv.pop(0)

    for globpath in glob.iglob(path, recursive=True):
        if os.path.isdir(globpath):
            continue

        with pynity.Archive(globpath) as archive:
            pprint(archive.header)
            pprint(archive.entries)
            extract_path, _ = os.path.splitext(globpath)
            archive.extract(extract_path)

if __name__ == "__main__":
    sys.exit(main(sys.argv))