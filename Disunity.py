import sys
import os
import glob
import json
from pprint import pprint

from SerializedFile import *

def process(path):
    with SerializedFile(path) as sf:
        if not sf.valid:
            return

        print(path)

        script_dir = os.path.dirname(__file__)
        types_dir = os.path.join(script_dir, "resources", "types")

        for path_id in sf.types.classes:
            if path_id <= 0:
                continue

            bclass = sf.types.classes[path_id]

            if "old_type_hash" in bclass:
                path_dir = os.path.join(types_dir, str(path_id))
                path_type = os.path.join(path_dir, bclass.old_type_hash + ".json")

                if bclass.type_tree:
                    if not os.path.exists(path_dir):
                        os.makedirs(path_dir)

                    if not os.path.exists(path_type):
                        print(path_type)
                        with open(path_type, "w") as file:
                            json.dump(bclass.type_tree, file, indent=2, separators=(',', ': '))
                else:
                    found = os.path.exists(path_type)
                    if not found:
                        print("% 4d %s" % (path_id, bclass.old_type_hash))

def main(argv):
    app = argv.pop(0)
    path = argv.pop(0)

    for globpath in glob.iglob(path):
        if os.path.isdir(globpath):
            continue

        process(globpath)

    return 0

if __name__ == "__main__":
    sys.exit(main(sys.argv))