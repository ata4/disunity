from collections import OrderedDict

class ObjectDict(OrderedDict):

    __getattr__ = OrderedDict.__getitem__
    __setattr__ = OrderedDict.__setitem__
    __delattr__ = OrderedDict.__delitem__