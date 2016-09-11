import os
import json
import hashlib
import logging

from collections import OrderedDict

log = logging.getLogger("pynity.typedb")

class TypeDatabase:

    def __init__(self):
        self.path_resources = os.path.join(os.path.dirname(__file__), "resources")
        self.path_types = os.path.join(self.path_resources, "types")
        self.path_types_old = os.path.join(self.path_resources, "types_old")
        self.cache = {}
        self.cache_old = {}

    def add(self, type_tree, class_id, hash):
        path_dir = os.path.join(self.path_types, str(class_id))
        if not os.path.exists(path_dir):
            os.makedirs(path_dir)

        # write missing type files
        path_type = os.path.join(path_dir, hash + ".json")
        if not os.path.exists(path_type):
            log.info("Added type " + hash)
            with open(path_type, "w") as fp:
                json.dump(type_tree, fp, indent=2, separators=(',', ': '))

    def add_old(self, type_tree, class_id, version):
        path_dir = os.path.join(self.path_types_old, str(class_id))
        if not os.path.exists(path_dir):
            os.makedirs(path_dir)

        path_index = os.path.join(path_dir, "index.json")

        # check if the version already has a hash
        index = self.VersionIndex(path_index)
        hash = index.get(version, exact=True)
        if hash:
            return

        # generate hash based on JSON string
        json_type_tree = json.dumps(type_tree, indent=2, separators=(',', ': '))
        json_type_tree_raw = json_type_tree.encode("utf-8")
        hash = hashlib.md5(json_type_tree_raw).hexdigest()

        # write missing type files
        path_type = os.path.join(path_dir, hash + ".json")
        if not os.path.exists(path_type):
            log.info("Added type " + hash)
            with open(path_type, "wb") as fp:
                fp.write(json_type_tree_raw)

        # update version index
        index.add(version, hash)

    def get(self, class_id, hash):
        # load from cache if possible
        if hash in self.cache:
            return self.cache[hash]

        path_type_dir = os.path.join(self.path_types, str(class_id))
        path_type = os.path.join(path_type_dir, hash + ".json")

        # bail out if the type file doesn't exist
        if not os.path.exists(path_type):
            log.warning("Type %s not found in file or database" % hash)
            self.cache[hash] = None
            return

        # load type file
        log.debug("Type %s loaded from database" % hash)

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
        path_index = os.path.join(path_type_dir, "index.json")

        index = self.VersionIndex(path_index)
        hash = index.get(version)

        # bail out if there's no match inside the index
        if not hash:
            log.warning("Type for class ID %d not found in file or database"
                        % class_id)
            self.cache_old[class_id] = None
            return

        # load type file
        path_type = os.path.join(path_type_dir, hash + ".json")

        log.debug("Type %s loaded from database" % hash)

        with open(path_type) as fp:
            type_tree = ObjectDict.from_dict(json.load(fp))
            self.cache_old[class_id] = type_tree
            return type_tree

    class VersionIndex:

        def __init__(self, path):
            self.path = path
            if os.path.exists(path):
                with open(path) as fp:
                    self.data = json.load(fp)
            else:
                self.data = {}

        def get(self, version, exact=False):
            # search for direct match
            hash = self.search(version)
            if hash or exact:
                return hash

            # search for major, minor and patch (first 5 chars)
            hash = self.search(version, 5)
            if hash:
                return hash

            # search for major and minor only (first 3 chars)
            hash = self.search(version, 3)
            if hash:
                return hash

        def search(self, version, num_chars=0):
            if num_chars == 0:
                for hash, versions in self.data.items():
                    if version in versions:
                        return hash
            else:
                for hash, versions in self.data.items():
                    for version_index in versions:
                        if version_index[:num_chars] == version[:num_chars]:
                            return hash

        def add(self, version, hash):
            # catch potential silly mistakes
            if len(hash) != 32:
                raise ValueError("Invalid hash string: " + hash)

            # cancel if version is already in index
            if hash in self.data and version in self.data[hash]:
                return

            # insert version and hash
            if hash in self.data:
                self.data[hash].append(version)
            else:
                self.data[hash] = [version]

            log.info("Added version %s to type hash %s" % (version, hash))

            # update index file
            self.save()

        def save(self):
            # sort version strings
            for versions in self.data.values():
                versions.sort()

            # sort keys by last version in list
            self.data = OrderedDict(sorted(self.data.items(), key=lambda t: t[1][-1]))

            # write file
            with open(self.path, "w") as fp:
                json.dump(self.data, fp, indent=2, separators=(',', ': '))