import sys
import io

import disunity
import pynity

class SerializeTest(disunity.CommandLineApp):

    def __init__(self):
        self.num_files_passed = 0
        self.num_files_failed = 0
        self.num_files_skipped = 0
        self.num_objects_passed = 0
        self.num_objects_failed = 0
        self.num_objects_typeless = 0

        self.scan_types = True
        self.deserialize = False

    def main(self, argv):
        super(SerializeTest, self).main(argv)

        print()
        print("Files passed:     %d" % self.num_files_passed)
        print("Files failed:     %d" % self.num_files_failed)
        print("Files skipped:    %d" % self.num_files_skipped)

        if self.deserialize:
            print("Objects passed:   %d" % self.num_objects_passed)
            print("Objects failed:   %d" % self.num_objects_failed)
            print("Objects typeless: %d" % self.num_objects_typeless)

    def process(self, path):
        try:
            if pynity.Archive.probe(path):
                with pynity.Archive(path) as archive:
                    for entry in archive.entries:
                        entry_data = archive.read(entry)
                        entry_file = io.BytesIO(entry_data)
                        if pynity.SerializedFile.probe_file(entry_file):
                            print(path + ":" + entry.path)
                            self.process_serialized(entry_file)
            elif pynity.SerializedFile.probe_path(path):
                print(path)
                self.process_serialized(path)
            else:
                self.num_files_skipped += 1
        except Exception:
            self.log.exception("Failed reading " + path)
            self.num_files_failed += 1

    def process_serialized(self, file):
        with pynity.SerializedFile(file) as sf:
            if self.scan_types:
                sf.scan_types()

            if self.deserialize:
                for path_id in sf.objects:
                    try:
                        object = sf.read_object(path_id)
                        if not object:
                            self.num_objects_typeless += 1
                            continue

                        object_name = object.m_Name if "m_Name" in object else ""
                        class_name = object.__class__.__name__
                        print(path_id, class_name, object_name)

                        self.num_objects_passed += 1
                    except Exception:
                        self.log.exception("Failed deserialization for path ID %d" % path_id)
                        self.num_objects_failed += 1

            self.num_files_passed += 1

if __name__ == "__main__":
    sys.exit(SerializeTest().main(sys.argv))