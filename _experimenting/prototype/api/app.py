import arrow
import threading

from flask import Flask, render_template, jsonify
from data.generator import DataFileProducer
from streaming.ml.classification.trees import HoeffdingTree, ClassicTree


app = Flask(__name__)

KAFKA_HOSTS = 'localhost:9092'
KAFKA_TOPIC = 'iris'


@app.route('/')
def index() -> str:
    return 'Hello world'


@app.route('/tree')
def tree_route() -> str:
    return render_template('tree.html')


@app.route('/tree-data/<string:tree_type>/')
def tree_data(tree_type: str) -> str:
    if tree_type == 'classic':
        t = ClassicTree()
        print('classic tree, samples: {}'.format(t.size))

        return t.run()

    return 'Select valid tree type!'


@app.route('/hfdt-test-run')
def api_hfdt_test_run() -> str:
    data_source = DataFileProducer(
        filename='iris.data.txt',
        bootstrap_servers=KAFKA_HOSTS,
        topic=KAFKA_TOPIC
    )
    data_source.run()

    ht = HoeffdingTree('test_hfdt', 0.1)
    ht.run(KAFKA_TOPIC)

    return 'Produced at {}'.format(arrow.now())


if __name__ == '__main__':
    app.run(port=8080)
