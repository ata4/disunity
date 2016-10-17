import os
import io
import json
import logging
import hashlib

from collections import OrderedDict

from . import utils, ioutils, stringtable

log = logging.getLogger("pynity.rtti")

def read_node(r):
    # read sizes
    num_fields = r.read_int32()
    string_table_len = r.read_int32()

    # read local string table first so the strings can be assigned in one go
    tree_pos = r.tell()
    tree_len = 24 * num_fields
    r.seek(tree_len, io.SEEK_CUR)
    string_table_buf = r.read(string_table_len)
    string_table = stringtable.load(string_table_buf)
    r.seek(tree_pos)

    # read type tree
    field_stack = []
    field_root = None

    for _ in range(num_fields):
        field = utils.ObjectDict()
        field.version = r.read_int16()

        level = r.read_uint8()

        # pop redundant entries from stack if required
        while len(field_stack) > level:
            field_stack.pop()

        # add current node as child for topmost (previous) node
        if field_stack:
            field_stack[-1].children.append(field)

        # add current node on top of stack
        field_stack.append(field)

        field.is_array = r.read_bool8()

        # assign type string
        type_offset = r.read_uint32()
        field.type = string_table.get(type_offset)
        if not field.type:
            raise TypeException("Invalid field type string offset: %d"
                                % type_offset)

        # assign name string
        name_offset = r.read_uint32()
        field.name = string_table.get(name_offset)
        if not field.name:
            raise TypeException("Invalid field name string offset: %d"
                                % name_offset)

        field.size = r.read_int32()
        field.index = r.read_int32()
        field.meta_flag = r.read_int32()
        field.children = []

        # save first node, which is the root
        if not field_root:
            field_root = field

    # correct end position
    r.seek(string_table_len, io.SEEK_CUR)

    return field_root

def read_node_old(r):
    field = utils.ObjectDict()
    field.type = r.read_cstring()
    field.name = r.read_cstring()
    field.size = r.read_int32()
    field.index = r.read_int32()
    field.is_array = r.read_bool32()
    field.version = r.read_int32()
    field.meta_flag = r.read_int32()
    field.children = []

    num_children = r.read_int32()
    for _ in range(num_children):
        field.children.append(read_node_old(r))

    return field

class Database:

    type_ext = ".unitytype"
    _cached_types = {}
    _cached_types_old = {}

    def __init__(self, path_resources=None):
        if path_resources:
            self.path_resources = path_resources
        else:
            self.path_resources = os.path.join(os.path.dirname(__file__), "resources")

        self.path_types = os.path.join(self.path_resources, "types")
        self.path_types_old = os.path.join(self.path_resources, "types_old")

        self.version = 0
        self.order = ioutils.LITTLE_ENDIAN

    def _type_open(self, path):
        r = ioutils.BinaryIO(open(path, "rb"))
        r.order = r.read_int8()

        # file version, currently unused but may be helpful in future
        r.read_int32()

        return r

    def _type_write(self, data, path):
        if os.path.exists(path):
            return False

        with ioutils.BinaryIO(open(path, "wb"), order=self.order) as w:
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
        if not sf.types.embedded:
            return types_added

        # save some metadata required to identify old types
        self.version = sf.header.version
        self.order = sf.r.order

        for class_id, class_type in sf.types.classes.items():
            # ignore script types
            if class_id <= 0:
                continue

            # read raw type data
            offset = class_type.offset
            sf.r.seek(offset[0])
            type_raw = sf.r.read(offset[1] - offset[0])

            # add types that don't exist yet
            if sf.header.version > 13:
                if self.add(type_raw, class_id, class_type.old_type_hash):
                    types_added += 1
            else:
                signature = sf.types.get("signature")
                if signature and self.add_old(type_raw, class_id, signature):
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

    def get(self, class_id, type_hash):
        key = (class_id, type_hash)

        if key not in self._cached_types:
            with self.open(class_id, type_hash) as fp:
                self._cached_types[key] = read_node(fp)

        return self._cached_types[key]

    def get_old(self, class_id, signature):
        key = (class_id, signature)

        if key not in self._cached_types_old:
            with self.open_old(class_id, signature) as fp:
                self._cached_types_old[key] = read_node_old(fp)

        return self._cached_types_old[key]

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
