import json
from kafka import KafkaConsumer
from app.utils.logger import logger
from app.kafka_client.producer import send_commentary  # 코멘터리 발송 함수 import

def start_consumer():
    try:
        consumer = KafkaConsumer(
            'highlight-event',
            bootstrap_servers='kafka:9092',
            auto_offset_reset='latest',
            enable_auto_commit=True,
            value_deserializer=lambda v: v.decode('utf-8')
        )

        logger.info("Kafka Consumer thread started.")
        logger.info("Listening for highlight-event messages...")

        for message in consumer:
            raw_value = message.value.strip()  # 공백 제거
            if not raw_value:
                continue  # 빈 메시지는 무시

            try:
                data = json.loads(raw_value)
                logger.info(f"Received Kafka event: {data}")

                # 코멘터리 생성 로직 실행
                highlight_type = data.get("highlightType")
                nickname = data.get("nickname")
                target = data.get("targetNickname")

                if highlight_type == "OVERTAKE":
                    commentary = f"{nickname}님이 {target}님을 추월했습니다!"
                elif highlight_type == "FINISH":
                    commentary = f"{nickname}님이 완주했습니다!"
                else:
                    commentary = f"{nickname}님의 {highlight_type} 이벤트 감지!"

                # Kafka Producer로 전달 (다른 서비스에 알림)
                send_commentary(commentary)
                logger.info(f"Commentary generated & sent: {commentary}")

            except json.JSONDecodeError as e:
                logger.error(f"JSON parse error: {e} / raw: {raw_value}")

    except Exception as e:
        logger.error(f"Kafka consumer failed to start: {e}")
