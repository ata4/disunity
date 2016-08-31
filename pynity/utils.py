from collections import OrderedDict

class ObjectDict(OrderedDict):

    __getattr__ = OrderedDict.__getitem__
    __setattr__ = OrderedDict.__setitem__
    __delattr__ = OrderedDict.__delitem__

    @staticmethod
    def from_dict(d):
        if isinstance(d, dict):
            return ObjectDict((k, ObjectDict.from_dict(v)) for k,v in d.items())
        elif isinstance(d, (list, tuple)):
            return type(d)(ObjectDict.from_dict(v) for v in d)
        else:
            return d