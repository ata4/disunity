import sys
import os
import pynity

def main(argv):
    app = argv.pop(0)

    if not argv:
        usage(app)
        return 1

    path = argv.pop(0)

    if argv:
        path_out = argv.pop(0)
    else:
        path_out, _ = os.path.splitext(path)

    with pynity.Archive(path) as archive:
        for entry in archive.entries:
            print(entry.path)
            archive.extract(path_out, [entry])

def usage(app):
    print("usage: %s <archive path> [output dir]" % os.path.basename(app))

if __name__ == "__main__":
    sys.exit(main(sys.argv))