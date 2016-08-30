from collections import OrderedDict

class ObjectDict(OrderedDict):

    __getattr__ = OrderedDict.__getitem__
    __setattr__ = OrderedDict.__setitem__
    __delattr__ = OrderedDict.__delitem__
    
class AutoCloseable:

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()

    def close(self):
        pass