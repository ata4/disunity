from .utils import ObjectDict

class Object(ObjectDict):

    @property
    def name(self):
        name_str = self.get("m_Name")
        if name_str is None:
            name_str = ""
        return name_str
