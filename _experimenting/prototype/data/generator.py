"""
This class generates synthetic data to Kafka
"""
import os
import threading

from kafka import KafkaProducer


class Data(object):

    def __init__(self, filename: str) -> None:
        self.file = '../datasets/{}'.format(filename)


class DataFileProducer(Data):
    """Reads file from disk and produces its content to desired Kafka topic"""

    def __init__(self, filename: str, **kwargs: str) -> None:
        super().__init__(filename)
        self.producer = KafkaProducer(bootstrap_servers=kwargs.get('bootstrap_servers', 'localhost:9092'))
        self.kafka_topic = kwargs.get('topic', 'topic_{}'.format(__name__))

    def __produce(self, kafka_topic=None):
        if os.stat(self.file).st_size == 0:
            raise IOError("File is empty!")

        topic = kafka_topic
        if topic is None:
            topic = self.kafka_topic

        file = open(self.file, 'r')
        for line in file:
            self.producer.send(topic, bytes(line, 'utf-8'))

        self.producer.close()
        print("Finished producing")

    def run(self):
        print("Producing into topic: {}".format(self.kafka_topic))  # TODO: change this for logger
        producer_thread = threading.Thread(target=self.__produce())
        producer_thread.start()