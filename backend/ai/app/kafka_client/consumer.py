import json
from kafka import KafkaConsumer
from app.utils.logger import logger
from app.kafka_client.producer import send_commentary  # 코멘터리 발송 함수 import

def start_consumer():
    bootstrap = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")

    consumer = KafkaConsumer(
        "highlight-event",
        bootstrap_servers=bootstrap,
        group_id="llm-commentary-group",
        auto_offset_reset="latest",
        enable_auto_commit=True,
        value_deserializer=lambda v: json.loads(v.decode("utf-8")),
    )

    logger.info(f"Listening for highlight-event... (bootstrap={bootstrap})")

    for msg in consumer:
        try:
            event = HighlightEvent(**msg.value)
            logger.info(f"Received Kafka event: {event}")

            # LLM/룰 기반으로 멘트 생성
            commentary = generate_commentary(event)
            event.commentary = commentary

            # commentary-event 토픽으로 발행
            send_commentary(event)

        except Exception as e:
            logger.error(f"[ERROR] Failed to handle highlight-event: {e}")