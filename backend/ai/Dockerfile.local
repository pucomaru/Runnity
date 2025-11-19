FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 8000

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]

# ---- Add Google TTS key ----
COPY google-tts-key.json /app/google-tts-key.json
ENV GOOGLE_APPLICATION_CREDENTIALS="/app/google-tts-key.json"