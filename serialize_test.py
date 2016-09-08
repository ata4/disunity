import sys
import os
import glob
import json
import logging

import pynity

log = logging.getLogger()

def main(argv):
    app = argv.pop(0)
    path = argv.pop(0)

    num_files_passed = 0
    num_files_failed = 0
    num_files_skipped = 0
    num_objects_passed = 0
    num_objects_failed = 0
    num_objects_typeless = 0

    for globpath in glob.iglob(path, recursive=True):
        if os.path.isdir(globpath):
            continue

        try:
            with pynity.SerializedFile(globpath) as sf:
                if not sf.valid:
                    num_files_skipped += 1
                    continue

                print(globpath)
                sf.scan_types()

                for path_id in sf.objects:
                    try:
                        object = sf.read_object(path_id)
                        if not object:
                            num_objects_typeless += 1
                            continue

                        object_name = object.m_Name if "m_Name" in object else ""
                        class_name = object.__class__.__name__
                        print(path_id, class_name, object_name)

                        num_objects_passed += 1
                    except Exception:
                        log.exception("Failed deserialization for path ID %d" % path_id)
                        num_objects_failed += 1

                num_files_passed += 1
        except Exception:
            log.exception("Failed reading " + globpath)
            num_files_failed += 1

    print()
    print("Files passed:     %d" % num_files_passed)
    print("Files failed:     %d" % num_files_failed)
    print("Files skipped:    %d" % num_files_skipped)
    print("Objects passed:   %d" % num_objects_passed)
    print("Objects failed:   %d" % num_objects_failed)
    print("Objects typeless: %d" % num_objects_typeless)

    return 0

if __name__ == "__main__":
    sys.exit(main(sys.argv))