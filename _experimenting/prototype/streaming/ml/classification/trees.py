"""This module contains tree classification algorithms

    Hoeffding tree
    Classic decision tree
"""
import tempfile
import json

from streaming.ml.classification.models.tree import Tree
from streaming.ml.classification.models.export import export_json, treeToJson
from kafka import KafkaConsumer
from sklearn.datasets import load_iris
from sklearn import tree


class Singleton(type):
    _instances = {}  # type: dict

    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            cls._instances[cls] = super(Singleton, cls).__call__(*args, **kwargs)
        return cls._instances[cls]


class HoeffdingTree(Tree, metaclass=Singleton):
    def __init__(self, name: str, delta: float) -> None:
        super().__init__()
        self.name = name
        self.delta = delta

    def run(self, topic):
        """Run Hoeffding algorithm reading data from Kafka topic"""
        print('Running HFDT algorithm')

        print('Consuming data from Kafka topic: {}'.format(topic))
        consumer = KafkaConsumer(topic)
        for sample in consumer:
            print('Training sample: {}'.format(sample.value))
            self._hfdt(sample)

        print('Done')

    def _hfdt(self, sample):
        pass



class ClassicTree(metaclass=Singleton):
    def __init__(self):
        self.iris = load_iris()
        self.clf = tree.DecisionTreeClassifier()
        self.size = 10  # init size

    def run(self):
        result_clf = self.clf.fit(self.iris.data[0:self.size], self.iris.target[0:self.size])

        out_file = treeToJson(result_clf, self.iris.feature_names)
        # out_file = tree.export_graphviz(result_clf, out_file=None,
        #                                 feature_names=self.iris.feature_names,
        #                                 class_names=self.iris.target_names,
        #                                 filled=True, rounded=True,
        #                                 special_characters=True)
        # out_file = export_json(result_clf, out_file=tempfile.TemporaryFile(),
        #                             feature_names=self.iris.feature_names)

        self.size += 10
        return out_file