import os
import io
import glob
import logging

import pynity

class RecursiveFileApp:

    def main(self, argv):
        self.log = logging.getLogger()
        self.path = argv.pop(0)

        if not self.parse_args(argv):
            self.usage()
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
        print("Usage: %s [FILE...]" % os.path.basename(self.path))

class SerializedFileApp(RecursiveFileApp):

    def process(self, path):
        if pynity.Archive.probe(path):
            with pynity.Archive(path) as archive:
                self.process_archive(path, archive)
        elif pynity.SerializedFile.probe_path(path):
            with pynity.SerializedFile(path) as sf:
                self.process_serialized(path, sf)

    def process_archive(self, path, archive):
        for entry in archive.entries:
            entry_data = archive.read(entry)
            entry_file = io.BytesIO(entry_data)
            if pynity.SerializedFile.probe_file(entry_file):
                with pynity.SerializedFile(entry_file) as sf:
                    self.process_serialized(path + ":" + entry.path, sf)

    def process_serialized(self, path, sf):
        pass