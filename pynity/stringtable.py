import os

def strmap(buf, offset=0):
    str_list = [string.decode("ascii") for string in buf.split(b'\0')]
    str_map = {}
    p = 0

    for string in str_list:
        if not string:
            continue

        str_map[p + offset] = string
        p += len(string) + 1
    return str_map

def load(buf):
    if load.strings_global is None:
        script_dir = os.path.dirname(__file__)
        strings_path = os.path.join(script_dir, "resources", "types", "common.unitystrings")

        with open(strings_path, "rb") as fp:
            load.strings_global = strmap(fp.read(), offset=1<<31)

    strings = load.strings_global.copy()
    strings.update(strmap(buf))
    return strings

load.strings_global = None