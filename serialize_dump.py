import sys
import os
import json
import pynity

from collections import OrderedDict

class JSONEncoderImpl(json.JSONEncoder):
    def default(self, o):
        return str(o)

def main(argv):
    cmds = {
        "header": lambda sf: json_dump(sf.header),
        "types": lambda sf: json_dump(sf.types),
        "object_info": lambda sf: json_dump(sf.objects),
        "script_types": lambda sf: json_dump(sf.script_types),
        "externals": lambda sf: json_dump(sf.externals),
        "objects": dump_objects
    }

    app = argv.pop(0)

    if len(argv) < 2:
        usage(app, cmds)
        return 1

    cmd = argv.pop(0)
    path = argv.pop(0)

    if not cmd in cmds:
        usage(app, cmds)
        return 1

    with pynity.SerializedFile(path) as sf:
        cmds[cmd](sf)

def usage(app, cmds):
    print("usage: %s <%s> <path>" % (os.path.basename(app), "|".join(cmds.keys())))

def dump_objects(sf):
    objects = []

    for path_id in sf.objects:
        object = sf.read_object(path_id)
        if not object:
            continue

        object_data = OrderedDict()
        object_data["path"] = path_id
        object_data["class"] = object.__class__.__name__
        object_data["object"] = object

        objects.append(object_data)

    json_dump(objects)

def json_dump(object):
    json.dump(object, sys.stdout, indent=2, separators=(',', ': '), cls=JSONEncoderImpl)

if __name__ == "__main__":
    sys.exit(main(sys.argv))