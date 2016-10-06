import sys
import struct
import lz4

import disunity

from pprint import pprint

class Extract(disunity.SerializedFileApp):

    def process_serialized(self, path, sf):
        for path_id, object_s in sf.objects.items():
            # TextAsset
            #if object_info.class_id in (49, 94, 109):
            #    object = sf.read_object(path_id)
            #    print(object.m_Name, object.m_PathName)

            #    with open(name, "w") as fp:
            #        fp.write(object.m_Script)

            # Shader
            if object_s._info.class_id == 48:
                object = object_s.instance
                print(object.m_Name, object.m_PathName)

                name = object.m_Name
                if not name:
                    name = str(path_id)

                with open(name + ".shader", "w") as fp:
                    fp.write(object.m_Script)

                if "m_SubProgramBlob" in object:
                    with open(name + ".shaderblob", "wb") as fp:
                        hdr = struct.pack("<I", object.decompressedSize)
                        blob = lz4.decompress(hdr + object.m_SubProgramBlob)
                        fp.write(blob)

            # AudioClip
            #if object_info.class_id == 83:
            #    object = sf.read_object(path_id)
            #    object_name = object.m_Name
            #    resource = object.m_Resource
            #    dir_path = os.path.dirname(path)
            #    resource_path = os.path.join(dir_path, resource.m_Source)
            #    resource_extract_path = os.path.join(dir_path, "AudioClip", object_name + ".fsb")

            #    offset = resource.m_Offset
            #    length = resource.m_Size
            #    bufsize = 4096

            #    try:
            #        print(resource_extract_path)
            #        with UnityFile(resource_path, 'rb') as f1:
            #            f1.seek(offset)
            #            with open(resource_extract_path, 'wb') as f2:
            #                while length:
            #                    chunk = min(bufsize, length)
            #                    data = f1.read(chunk)
            #                    f2.write(data)
            #                    length -= chunk
            #    except FileNotFoundError:
            #        pass

if __name__ == "__main__":
    sys.exit(Extract().main(sys.argv))