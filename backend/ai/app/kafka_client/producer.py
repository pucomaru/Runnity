from kafka import KafkaProducer
import json
from app.utils.logger import logger
import os
from app.models.highlight_event import HighlightEvent
from app.llm.tts import generate_tts_mp3


# commentary-event 발행

# ======================
# Kafka Producer (Lazy)
# ======================

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
   
# ======================
# Commentary Sender
# ======================
def send_commentary(event: HighlightEvent):

    # 1) Producer 가져오기
    producer = _get_producer()
    if producer is None:
        logger.error("Producer unavailable. Cannot send commentary.")
        return

    # 2) 텍스트 코멘터리
    text = event.commentary or ""

    # 3) Google TTS 음성 생성
    try:
        audio_path = generate_tts_mp3(text)
    except Exception as e:
        logger.error(f"TTS generation failed: {e}")
        audio_path = None

    # 4) Kafka 전송 데이터 구성
    data = event.dict()
    data["audio_path"] = audio_path

    # 5) Kafka 발행
    try:
        producer.send("commentary-event", value=data)
        producer.flush()
    except Exception as e:
        logger.error(f"Kafka send error: {e}")
        return

    logger.info(f"[COMMENTARY SENT] {text} (TTS={audio_path})")