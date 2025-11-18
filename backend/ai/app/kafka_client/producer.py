from kafka import KafkaProducer
import json
from app.utils.logger import logger
import os
from app.models.highlight_event import HighlightEvent
# from app.llm.tts import generate_tts_mp3
from app.models.llm_commentary import LLMCommentaryMessage

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

    # Stream서버 규격에 맞는 LLM 메시지 생성
    llm_msg = LLMCommentaryMessage(
        challengeId=event.challengeId,
        runnerId=event.runnerId,
        nickname=event.nickname,
        profileImage=event.profileImage,

        highlightType=event.highlightType,
        commentary=text,

        targetRunnerId=event.targetRunnerId,
        targetNickname=event.targetNickname,
        targetProfileImage=event.targetProfileImage,

        distance=event.distance,
        pace=event.pace,
        ranking=event.rank
    )

    # 4) Kafka 전송 데이터 구성
    data = llm_msg.dict()
    # data["audio_path"] = audio_path

    # 5) Kafka 발행
    try:
        producer.send("highlight-llm-commentary", value=data)
        producer.flush()
    except Exception as e:
        logger.error(f"Kafka send error: {e}")
        return

    logger.info(f"[COMMENTARY SENT] {text}")
