from kafka import KafkaProducer
import json
from app.utils.logger import logger
import os
from app.models.highlight_event import HighlightEvent

# commentary-event 발행

_bootstrap = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")


_producer = None   # Lazy init

def _get_producer():
    global _producer
    if _producer is None:
        bootstrap = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")
        try:
            _producer = KafkaProducer(
                bootstrap_servers=bootstrap,
                value_serializer=lambda v: json.dumps(v, ensure_ascii=False).encode("utf-8"),
                retries=3,
                api_version_auto_timeout_ms=5000
            )
            logger.info(f"KafkaProducer initialized (bootstrap={bootstrap})")
        except Exception as e:
            logger.error(f"Failed to initialize KafkaProducer: {e}")
            _producer = None
    return _producer


def send_commentary(event):
    producer = _get_producer()
    if producer is None:
        logger.error("Producer unavailable! Cannot send commentary.")
        return

    data = event.dict()
    producer.send("commentary-event", value=data)
    producer.flush()
    logger.info(f"[COMMENTARY SENT] {data.get('commentary')}")