from collections import OrderedDict

class ObjectDict(OrderedDict):

    __getattr__ = OrderedDict.__getitem__
    __setattr__ = OrderedDict.__setitem__
    __delattr__ = OrderedDict.__delitem__

    @classmethod
    def from_dict(cls, d):
        if isinstance(d, dict):
            return cls((k, cls.from_dict(v)) for k,v in d.items())
        elif isinstance(d, (list, tuple)):
            return type(d)(cls.from_dict(v) for v in d)
        else:
            return d
