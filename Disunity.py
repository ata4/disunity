import sys
import os
import glob
import json
from pprint import pprint

from SerializedFileReader import *

def process(sf):
    script_dir = os.path.dirname(__file__)
    types_dir = os.path.join(script_dir, "resources", "types")

    for path_id in sf.types.classes:
        if path_id <= 0:
            continue
        bclass = sf.types.classes[path_id]

        path_dir = os.path.join(types_dir, str(path_id))
        path_type = os.path.join(path_dir, bclass.old_type_hash.hex + ".json")

        if "type_tree" in bclass:
            if not os.path.exists(path_dir):
                os.makedirs(path_dir)

            if not os.path.exists(path_type):
                print(path_type)
                with open(path_type, "w") as file:
                    json.dump(bclass.type_tree, file, indent=2, separators=(',', ': '))
        else:
            found = os.path.exists(path_type)
            print(path_id, bclass.old_type_hash.hex, found)

def main(argv):
    app = argv.pop(0)
    path = argv.pop(0)

    reader = SerializedFileReader()

    for globpath in glob.iglob(path):
        if os.path.isdir(globpath):
            continue

        sf = reader.read_file(globpath)
        if sf:
            print(globpath)
            process(sf)

    return 0

if __name__ == "__main__":
    sys.exit(main(sys.argv))