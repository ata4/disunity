import sys
import os
import pynity

def main(argv):
    app = argv.pop(0)

    if not argv:
        usage(app)
        return 1

    path = argv.pop(0)

    with pynity.Archive(path) as archive:
        extract_path, _ = os.path.splitext(path)
        for entry in archive.entries:
            print(entry.path)
            archive.extract(extract_path, [entry])

def usage(app):
    print("usage: %s <path>" % os.path.basename(app))

if __name__ == "__main__":
    sys.exit(main(sys.argv))