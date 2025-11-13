import json
from kafka import KafkaConsumer
from app.utils.logger import logger
from app.kafka_client.producer import send_commentary  # 코멘터리 발송 함수 import
import os
from app.models.highlight_event import HighlightEvent
from app.llm.generator import generate_commentary

def start_consumer():
    bootstrap = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")

    try:
        consumer = KafkaConsumer(
            "highlight-event",
            bootstrap_servers=bootstrap,
            auto_offset_reset="latest",
            enable_auto_commit=True,
            value_deserializer=lambda v: v.decode("utf-8")
        )

        logger.info("Kafka Consumer thread started.")
        logger.info("Listening for highlight-event messages...")

        for message in consumer:
            raw = message.value.strip()
            if not raw:
                continue

            try:
                data = json.loads(raw)  # json으로 파싱
                event = HighlightEvent(**data)  # pydantic model로 변환

                # 1) LLM 멘트 생성
                commentary_text = generate_commentary(event)

                # 2) event.commentary 필드에 담기
                event.commentary = commentary_text

                logger.info(f"Received Kafka event: {event}")

                # 3) Kafka로 publish
                send_commentary(event)

            except Exception as e:
                logger.error(f"Consumer JSON error: {e} / raw: {raw}")

    except Exception as e:
        logger.error(f"Kafka consumer failed to start: {e}")