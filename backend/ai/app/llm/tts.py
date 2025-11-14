from google.cloud import texttospeech
import uuid
import os

def generate_tts_mp3(text: str, voice_name="ko-KR-Standard-D") -> str:
    """
    Google TTS → mp3 파일 생성 후 로컬에 저장
    결과 파일 경로 반환
    """

    client = texttospeech.TextToSpeechClient()

    synthesis_input = texttospeech.SynthesisInput(text=text)

    voice = texttospeech.VoiceSelectionParams(
        language_code="ko-KR",
        name=voice_name,
    )

    audio_config = texttospeech.AudioConfig(
        audio_encoding=texttospeech.AudioEncoding.MP3
    )

    response = client.synthesize_speech(
        input=synthesis_input,
        voice=voice,
        audio_config=audio_config
    )

    filename = f"/app/tts_output/{uuid.uuid4().hex}.mp3"

    os.makedirs("/app/tts_output", exist_ok=True)

    with open(filename, "wb") as out:
        out.write(response.audio_content)

    return filename