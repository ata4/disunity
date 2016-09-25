import sys
import os
import json
import collections
import base64
import textwrap

import disunity

class SerializeDump(disunity.SerializedFileApp):

    def __init__(self):
        super(SerializeTest, self).__init__()

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
        if self.cmd not in self.cmds:
            return False

        return len(argv) > 0

    def usage(self):
        cmds = "|".join(self.cmds.keys())
        print("Usage: %s [%s] [FILE...]" % (os.path.basename(self.path), cmds))

    def dump_objects(self, sf):
        objects = []

        for path_id, obj in sf:
            obj_data = collections.OrderedDict()
            obj_data["path"] = path_id
            obj_data["class"] = obj.__class__.__name__
            obj_data["object"] = obj

            objects.append(obj_data)

        self.json_dump(objects)

    def json_dump(self, obj):
        class JSONEncoderImpl(json.JSONEncoder):
            def default(self, o):
                if isinstance(o, (bytearray, bytes)):
                    # encode binary data to base64
                    return textwrap.wrap(base64.b64encode(o).decode("ascii"))
                else:
                    # use default string representation
                    return str(o)

        json.dump(obj, sys.stdout, indent=2, cls=JSONEncoderImpl)

if __name__ == "__main__":
    sys.exit(SerializeDump().main(sys.argv))
