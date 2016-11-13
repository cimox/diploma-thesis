

class Tree(object):

    def __init__(self) -> None:
        self.root = Node()


class Node(object):

    def __init__(self, **kwargs: object) -> None:
        if not kwargs:
            self.leaf = True
            self.instances_seen = 0
