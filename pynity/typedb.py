import os
import json
import logging

from collections import OrderedDict

from .utils import ObjectDict

log = logging.getLogger("pynity.typedb")

class TypeDatabase:

    cache = {}
    cache_old = {}

    def __init__(self):
        self.path_resources = os.path.join(os.path.dirname(__file__), "resources")
        self.path_types = os.path.join(self.path_resources, "types")
        self.path_types_old = os.path.join(self.path_resources, "types_old")

    def add(self, type_tree, class_id, hash):
        path_dir = os.path.join(self.path_types, str(class_id))
        if not os.path.exists(path_dir):
            os.makedirs(path_dir)

        # write missing type files
        path_type = os.path.join(path_dir, hash + ".json")
        if not os.path.exists(path_type):
            log.info("Added type %s for class %d" % (hash, class_id))
            with open(path_type, "w") as fp:
                json.dump(type_tree, fp, indent=2)
            return True
        else:
            return False

    def add_old(self, type_tree, class_id, hash, version):
        path_dir = os.path.join(self.path_types_old, str(class_id))
        if not os.path.exists(path_dir):
            os.makedirs(path_dir)

        # check if the version already has a hash
        index = self.VersionIndex(path_dir, class_id)
        if index.get(version, exact=True):
            return False

        # write missing type files
        path_type = os.path.join(path_dir, hash + ".json")
        if not os.path.exists(path_type):
            log.info("Added type %s for class %d" % (hash, class_id))
            with open(path_type, "w") as fp:
                json.dump(type_tree, fp, indent=2)
            return True

        # update version index
        if index.add(version, hash):
            return True

        return False

    def get(self, class_id, hash):
        # load from cache if possible
        if hash in self.cache:
            return self.cache[hash]

        path_type_dir = os.path.join(self.path_types, str(class_id))
        path_type = os.path.join(path_type_dir, hash + ".json")

        # bail out if the type file doesn't exist
        if not os.path.exists(path_type):
            log.warning("Type %s for class %d not found in file or database" %
                        (hash, class_id))
            self.cache[hash] = None
            return

        # load type file
        log.debug("Type %s for class %d loaded from database" % (hash, class_id))

        with open(path_type) as fp:
            type_tree = ObjectDict.from_dict(json.load(fp))
            self.cache[hash] = type_tree
            return type_tree

    def get_old(self, class_id, version):
        # script types are never saved in database
        if class_id < 0:
            return

        # load from cache if possible
        if class_id in self.cache_old:
            return self.cache_old[class_id]

        # load version index and find type hash for version
        path_type_dir = os.path.join(self.path_types_old, str(class_id))

        index = self.VersionIndex(path_type_dir, class_id)
        hash = index.get(version)

        # bail out if there's no match inside the index
        if not hash:
            log.warning("Type for class %d not found in file or database"
                        % class_id)
            self.cache_old[class_id] = None
            return

        # load type file
        path_type = os.path.join(path_type_dir, hash + ".json")

        log.debug("Type %s for class %d loaded from database" % (hash, class_id))

        with open(path_type) as fp:
            type_tree = ObjectDict.from_dict(json.load(fp))
            self.cache_old[class_id] = type_tree
            return type_tree

    class VersionIndex:

        def __init__(self, dir, class_id):
            self.class_id = class_id
            self.path = os.path.join(dir, "index.json")
            if os.path.exists(self.path):
                with open(self.path) as fp:
                    self.data = json.load(fp)
            else:
                self.data = {}

        def get(self, version, exact=False):
            # search for direct match
            hash, version_match = self.search(version)
            if hash or exact:
                return hash

            # search for major, minor and patch (first 5 chars)
            hash, version_match = self.search(version, 5)
            if hash:
                return hash

            # search for major and minor only (first 3 chars)
            hash, version_match = self.search(version, 3)
            if hash:
                log.warn("Using database type %s for version %s instead of %s, "
                         "deserializing objects using class %d may fail!"
                         % (hash, version_match, version, self.class_id))
                return hash

        def search(self, version, num_chars=0):
            if num_chars == 0:
                # search for exact version
                for hash, versions in self.data.items():
                    if version in versions:
                        return hash, version
            else:
                # search for version substring
                for hash, versions in self.data.items():
                    for version_index in versions:
                        if version_index[:num_chars] == version[:num_chars]:
                            return hash, version

            return None, None

        def add(self, version, hash):
            # catch potential silly mistakes
            assert len(hash) == 32

            # cancel if version is already in index
            if hash in self.data and version in self.data[hash]:
                return False

            # insert version and hash
            if hash in self.data:
                self.data[hash].append(version)
            else:
                self.data[hash] = [version]

            log.info("Added version %s to type %s for class %d" %
                     (version, hash, self.class_id))

            # update index file
            self.save()

            return True

        def save(self):
            # sort version strings
            for versions in self.data.values():
                versions.sort()

            # sort keys by last version in list
            self.data = OrderedDict(sorted(self.data.items(), key=lambda t: t[1][-1]))

            # write file
            with open(self.path, "w") as fp:
                json.dump(self.data, fp, indent=2)