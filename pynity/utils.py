from collections import OrderedDict
from munch import Munch

# tiny modification of Munch that uses OrderedDict instead of dict
class OrderedMunch(Munch, OrderedDict):
    pass
