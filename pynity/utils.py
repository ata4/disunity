from collections import OrderedDict

class ObjectDict(OrderedDict):

    __getattr__ = OrderedDict.__getitem__
    __setattr__ = OrderedDict.__setitem__
    __delattr__ = OrderedDict.__delitem__

    @classmethod
    def from_dict(cls, dict_src):
        if isinstance(dict_src, dict):
            return cls((k, cls.from_dict(v)) for k, v in dict_src.items())
        elif isinstance(dict_src, (list, tuple)):
            return type(dict_src)(cls.from_dict(v) for v in dict_src)
        else:
            return dict_src
