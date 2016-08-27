import sys
import os
import glob
from pprint import pprint

from ChunkedFileIO import *
from SerializedFileReader import *

def main(argv):
    app = argv.pop(0)
    path = argv.pop(0)

    reader = SerializedFileReader()

    for globpath in glob.iglob(path):
        if os.path.isdir(globpath):
            continue

        fname, fext = os.path.splitext(globpath)
        if fext == ".resource":
            continue

        if fext == ".split0":
            index = 0
            splitpath = fname + fext
            splitpaths = []

            while os.path.exists(splitpath):
                splitpaths.append(splitpath)
                index += 1
                splitpath = fname + ".split%d" % index

            print(splitpaths[0])
            with ChunkedFileIO(splitpaths) as file:
                sf = reader.read(file)
                pprint(sf)
        elif fext[0:6] == ".split":
            continue
        else:
            print(globpath)
            with open(globpath, "rb") as file:
                sf = reader.read(file)
                pprint(sf)

    return 0

if __name__ == "__main__":
    sys.exit(main(sys.argv))