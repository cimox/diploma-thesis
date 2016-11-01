from flask import Flask
from data_app.generator import DataFileProducer

app = Flask(__name__)


@app.route('/')
def index() -> str:
    return 'Hello world'


if __name__ == '__main__':
    dp = DataFileProducer(
        'iris.data.txt',
        bootstrap_servers = 'localhost:9092',
        topic = 'iris'
    )
    dp.produce()

    app.run(port=8080)