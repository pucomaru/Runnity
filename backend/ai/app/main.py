from fastapi import FastAPI
from app.kafka_client.consumer import start_consumer
from app.utils.logger import logger
import threading

app = FastAPI(title = "LLM Commentary Server")

@app.get("/")
def health_check():
    return {"status": "ok", "message": "LLM Commentary Server running"}

# kafka Consumer 별도 스레드로 실행
@app.on_event("startup")
def startup_event():
    thread = threading.Thread(target=start_consumer, daemon=True)
    thread.start()
    logger.info(" Kafka Consumer thread started.")
    