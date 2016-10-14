import os
import json
import logging
import hashlib

from collections import OrderedDict

from .io import BinaryIO, ByteOrder

log = logging.getLogger("pynity.typedb")

class TypeDatabase:

    type_ext = ".unitytype"

    def __init__(self, path_resources=None):
        if path_resources:
            self.path_resources = path_resources
        else:
            self.path_resources = os.path.join(os.path.dirname(__file__), "resources")

        self.path_types = os.path.join(self.path_resources, "types")
        self.path_types_old = os.path.join(self.path_resources, "types_old")

        self.version = 0
        self.order = ByteOrder.LITTLE_ENDIAN

    def _type_open(self, path):
        r = BinaryIO(open(path, "rb"))
        r.order = ByteOrder(r.read_int8())

        # file version, currently unused but may be helpful in future
        r.read_int32()

        return r

    def _type_write(self, data, path):
        if os.path.exists(path):
            return False

        with BinaryIO(open(path, "wb"), order=self.order) as w:
            w.write_int8(int(self.order))
            w.write_int32(self.version)
            w.write(data)

        return True

    def add(self, type_tree_raw, class_id, type_hash):
        if class_id < 0:
            return False

        path_dir = os.path.join(self.path_types, str(class_id))
        if not os.path.exists(path_dir):
            os.makedirs(path_dir)

        # write missing type files
        path_type = os.path.join(path_dir, type_hash + self.type_ext)
        added = self._type_write(type_tree_raw, path_type)
        if added:
            log.info("Added type %s for class %d", type_hash, class_id)

        return added

    def add_old(self, type_tree_raw, class_id, signature):
        if class_id < 0:
            return False

        path_dir = os.path.join(self.path_types_old, str(class_id))
        if not os.path.exists(path_dir):
            os.makedirs(path_dir)

        # check if the signature already has a hash
        index = self.SignatureIndex(path_dir, class_id)
        if index.get(signature, exact=True):
            return False

        type_hash = hashlib.md5(type_tree_raw).hexdigest()

        # write missing type files
        path_type = os.path.join(path_dir, type_hash + self.type_ext)
        added = self._type_write(type_tree_raw, path_type)
        if added:
            log.info("Added type %s for class %d", type_hash, class_id)

        # update signature index
        if index.add(signature, type_hash):
            added = True

        return added

    def add_all(self, sf):
        types_added = 0

        # skip scan entirely if there are no embedded types
        if not sf.types_raw:
            return types_added

        # save some metadata required to identify old types
        self.version = sf.header.version
        self.order = sf.r.order

        for class_id in sf.types.classes:
            # ignore script types
            if class_id <= 0:
                continue

            # ignore types that aren't loaded
            if class_id not in sf.types_raw:
                continue

            class_type = sf.types.classes[class_id]

            # add types that don't exist yet
            if sf.header.version > 13:
                if self.add(sf.types_raw[class_id], class_id,
                                class_type.old_type_hash):
                    types_added += 1
            else:
                signature = sf.types.get("signature")
                if (class_type and signature and
                        self.add_old(sf.types_raw[class_id], class_id, signature)):
                    types_added += 1

        return types_added

    def open(self, class_id, type_hash):
        path_type_dir = os.path.join(self.path_types, str(class_id))
        path_type = os.path.join(path_type_dir, type_hash + self.type_ext)

        # bail out if the type file doesn't exist
        if not os.path.exists(path_type):
            raise TypeException("Type %s for class %d not found in file or database"
                                % (type_hash, class_id))

        # open type file
        log.debug("Type %s for class %d loaded from database", type_hash, class_id)

        return self._type_open(path_type)

    def open_old(self, class_id, signature):
        # script types are never saved in database
        if class_id < 0:
            return

        # load signature index and find type hash for signature
        path_type_dir = os.path.join(self.path_types_old, str(class_id))

        index = self.SignatureIndex(path_type_dir, class_id)
        type_hash = index.get(signature)

        # bail out if there's no match inside the index
        if not type_hash:
            raise TypeException("Type for class %d not found in file or database"
                                % class_id)

        # open type file
        path_type = os.path.join(path_type_dir, type_hash + self.type_ext)

        log.debug("Type %s for class %d loaded from database", type_hash, class_id)

        return self._type_open(path_type)

    class SignatureIndex:

        def __init__(self, dir, class_id):
            self.class_id = class_id
            self.path = os.path.join(dir, "index.json")
            if os.path.exists(self.path):
                with open(self.path) as fp:
                    self.data = json.load(fp)
            else:
                self.data = {}

        def get(self, signature, exact=False):
            # search for direct match
            type_hash, signature_match = self.search(signature)
            if type_hash or exact:
                return type_hash

            # search for major, minor and patch (first 5 chars)
            type_hash, signature_match = self.search(signature, 5)
            if type_hash:
                return type_hash

            # search for major and minor only (first 3 chars)
            type_hash, signature_match = self.search(signature, 3)
            if type_hash:
                log.warning("Using database type %s for signature %s instead of %s, "
                            "deserializing objects using class %d may fail!",
                            type_hash, signature_match, signature, self.class_id)
                return type_hash

        def search(self, signature, num_chars=0):
            if num_chars == 0:
                # search for exact signature
                for type_hash, signatures in self.data.items():
                    if signature in signatures:
                        return type_hash, signature
            else:
                # search for signature substring
                for type_hash, signatures in self.data.items():
                    for signature_index in signatures:
                        if signature_index[:num_chars] == signature[:num_chars]:
                            return type_hash, signature_index

            return None, None

        def add(self, signature, type_hash):
            # catch potential silly mistakes
            assert len(type_hash) == 32

            # cancel if signature is already in index
            if type_hash in self.data and signature in self.data[type_hash]:
                return False

            # insert signature and hash
            if type_hash in self.data:
                self.data[type_hash].append(signature)
            else:
                self.data[type_hash] = [signature]

            log.info("Added signature %s to type %s for class %d",
                     signature, type_hash, self.class_id)

            # update index file
            self.save()

            return True

        def save(self):
            # sort signature strings
            for signatures in self.data.values():
                signatures.sort()

            # sort keys by last signature in list
            self.data = OrderedDict(sorted(self.data.items(), key=lambda t: t[1][-1]))

            # write file
            with open(self.path, "w") as fp:
                json.dump(self.data, fp, indent=2)

class TypeException(Exception):
    pass
