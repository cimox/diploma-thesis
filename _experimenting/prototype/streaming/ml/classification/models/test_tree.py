import unittest

from streaming.ml.classification.models.tree import Tree, Node


class TestTree(unittest.TestCase):

    def test_construct_empty(self):
        t = Tree()
        self.assertTrue(isinstance(t.root, Node))
        self.assertTrue(t.root.leaf)
        self.assertEqual(t.root.instances_seen, 0)


class TestNode(unittest.TestCase):

    def test_construct_empty(self):
        n = Node()
        self.assertTrue(n.leaf)
        self.assertEqual(n.instances_seen, 0)
        self.assertIs(n.instances_seen, 0)


if __name__ == '__main__':
    unittest.main()