FROM python:3.11-slim

WORKDIR /app

# 의존성 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 앱 코드
COPY . .

# Google TTS 키는 컨테이너 실행 시 volume으로 주입
ENV GOOGLE_APPLICATION_CREDENTIALS="/app/google-tts-key.json"

EXPOSE 8000

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
