import os
import glob
import logging

class CommandLineApp:

    def main(self, argv):
        self.log = logging.getLogger()
        self.path = argv.pop(0)

        if not self.parse_args(argv):
            print(self.usage())
            return 1

        for path in argv:
            for subpath in glob.iglob(path, recursive=True):
                if not os.path.isfile(subpath):
                    continue

                self.process(subpath)

    def parse_args(self, argv):
        return len(argv) > 0

    def process(self, path):
        pass

    def usage(self):
        return "usage: %s <file>" % os.path.basename(self.path)
