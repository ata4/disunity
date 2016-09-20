import sys
import os
import json
import collections
import base64
import textwrap

import disunity
import pynity

class SerializeDump(disunity.SerializedFileApp):

    def __init__(self):
        self.cmds = {
            "header": lambda sf: self.json_dump(sf.header),
            "types": lambda sf: self.json_dump(sf.types),
            "object_info": lambda sf: self.json_dump(sf.objects),
            "script_types": lambda sf: self.json_dump(sf.script_types),
            "externals": lambda sf: self.json_dump(sf.externals),
            "objects": self.dump_objects
        }

        self.cmd = ""

    def process_serialized(self, path, sf):
        print(path)
        self.cmds[self.cmd](sf)

    def parse_args(self, argv):
        if not argv:
            return False

        self.cmd = argv.pop(0)
        if not self.cmd in self.cmds:
            return False

        return len(argv) > 0

    def usage(self):
        return "usage: %s <%s> <path>" % (
            os.path.basename(self.path), "|".join(self.cmds.keys()))

    def dump_objects(self, sf):
        objects = []

        for path_id in sf.objects:
            object = sf.read_object(path_id)
            if not object:
                continue

            object_data = collections.OrderedDict()
            object_data["path"] = path_id
            object_data["class"] = object.__class__.__name__
            object_data["object"] = object

            objects.append(object_data)

        self.json_dump(objects)

    def json_dump(self, object):
        class JSONEncoderImpl(json.JSONEncoder):
            def default(self, o):
                if type(o) in [bytearray, bytes]:
                    # encode binary data to base64
                    return textwrap.wrap(base64.b64encode(o).decode("ascii"))
                else:
                    # use default string representation
                    return str(o)

        json.dump(object, sys.stdout, indent=2, cls=JSONEncoderImpl)

if __name__ == "__main__":
    sys.exit(SerializeDump().main(sys.argv))