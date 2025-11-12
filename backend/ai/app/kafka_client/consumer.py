from kafka import KafkaConsumer
from app.kafka_client.producer import send_commentary
from app.llm.generator import generate_commentary
from app.utils.logger import logger
import json, os

def start_consumer():
    bootstrap = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")
    logger.info(f"Connecting to Kafka bootstrap servers: {bootstrap}")
    try:
        consumer = KafkaConsumer(
            "highlight-event",
            bootstrap_servers=bootstrap,
            group_id="llm-commentary-group",
            value_deserializer=lambda m: json.loads(m.decode("utf-8")),
            auto_offset_reset="latest"
        )
        logger.info(f"Listening for highlight-event... (bootstrap={bootstrap})")

        for message in consumer:
            event = message.value
            logger.info(f"[HIGHLIGHT RECEIVED] {event}")
            commentary = generate_commentary(event)
            event["commentary"] = commentary
            send_commentary(event)

    except Exception as e:
        logger.error(f"Kafka consumer failed to start: {e}")
