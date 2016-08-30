import sys
import os
import glob
import json
from pprint import pprint

import pynity

def main(argv):
    app = argv.pop(0)
    path = argv.pop(0)

    for globpath in glob.iglob(path):
        if os.path.isdir(globpath):
            continue

        with pynity.SerializedFile(path) as sf:
            if not sf.valid:
                return

            print(path)
            sf.scan_types()

            for path_id in sf.objects:
                object = sf.read_object(path_id)
                if not object:
                    continue
                object_name = object.m_Name if "m_Name" in object else ""
                class_name = object.__class__.__name__
                print(path_id, class_name, object_name)
                #object_json = json.dumps(object, indent=2, separators=(',', ': '))
                #print(object_json)

    return 0

if __name__ == "__main__":
    sys.exit(main(sys.argv))