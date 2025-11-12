from kafka import KafkaProducer
import json
from app.utils.logger import logger
import os

# commentary-event 발행

_producer = None

def _get_producer():
    global _producer
    if _producer is None:
        bootstrap = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
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
    producer = _get_producer()
    if producer is None:
        logger.error("KafkaProducer unavailable; skipping send_commentary")
        return
    producer.send("commentary-event", value=event)
    producer.flush()
    logger.info(f"[COMMENTARY SENT] {event}")