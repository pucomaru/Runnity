from kafka import KafkaProducer
import json
from app.utils.logger import logger
import os
from app.models.highlight_event import HighlightEvent

# commentary-event 발행

_bootstrap = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")

producer = KafkaProducer(
    bootstrap_servers=_bootstrap,
    value_serializer=lambda v: json.dumps(v, ensure_ascii=False).encode("utf-8"),
)

def _get_producer():
    global _producer
    if _producer is None:
        bootstrap = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")
        try:
            _producer = KafkaProducer(
                bootstrap_servers=bootstrap,
                value_serializer=lambda v: json.dumps(v).encode("utf-8")
            )
            logger.info(f"KafkaProducer initialized (bootstrap={bootstrap})")
        except Exception as e:
            logger.error(f"Failed to initialize KafkaProducer: {e}")
            _producer = None
    return _producer

def send_commentary(event):
    data = event.dict()
    producer.send("commentary-event", value=data)
    producer.flush()
    logger.info(f"[COMMENTARY SENT] {data.get('commentary')}")