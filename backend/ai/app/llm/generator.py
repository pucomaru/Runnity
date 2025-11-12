import os
import random
import requests
from app.models.highlight_event import HighlightEvent
from app.utils.logger import logger

GMS_API_KEY = os.getenv("GMS_API_KEY")
GMS_CHAT_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions"

# 멘트 생성 로직
def generate_commentary(event: HighlightEvent) -> str:
    """
    1순위: GMS LLM 호출해서 문장 생성
    2순위: 룰 기반 fallback
    """
    # 1) LLM 사용 가능하면 먼저 시도
    if GMS_API_KEY:
        try:
            text = _generate_with_llm(event)
            if text:
                logger.info(f"[LLM COMMENTARY] {text}")
                return text
        except Exception as e:
            logger.error(f"LLM 호출 실패, fallback 사용: {e}")

    # 2) 실패하면 룰 기반으로
    text = _generate_rule_based(event)
    logger.info(f"[RULE COMMENTARY] {text}")
    return text


def _generate_with_llm(event: HighlightEvent) -> str:
    """
    GMS → OpenAI chat completions 형식으로 호출
    """
    nickname = event.nickname
    t = event.highlightType
    target = event.targetNickname or "상대"

    # 프롬프트 한국어로 빡세게!!!
    prompt = f"""
너는 러닝 챌린지 실시간 중계 아나운서야.
다음 이벤트를 보고 참가자에게 한 줄 멘트를 해줘.

이벤트 유형: {t}
참가자 닉네임: {nickname}
대상자 닉네임: {target if event.targetNickname else '없음'}
현재 순위: {event.rank}
이벤트 설명: 예) 추월, 결승선 통과, TOP3 진입 등

요구사항:
- 반드시 한국어 한 줄만 출력
- 문장 끝에 마침표 또는 느낌표로 끝내기
- 너무 길지 않게 (25자~40자 정도)
- 요즘 MZ 밈 스타일, 텐션 높게, 존댓말/반말 섞어도 됨
"""

    headers = {
        "Authorization": f"Bearer {GMS_API_KEY}",
        "Content-Type": "application/json",
    }

    payload = {
        "model": "gpt-5-mini",  # or gpt-4o-mini 등 GMS에서 허용하는 모델
        "messages": [
            {"role": "system", "content": "너는 러닝 대회 중계 캐스터다."},
            {"role": "user", "content": prompt},
        ],
        "max_tokens": 80,
        "temperature": 1.1,
    }

    resp = requests.post(GMS_CHAT_URL, headers=headers, json=payload, timeout=10)
    resp.raise_for_status()
    data = resp.json()

    # GMS 프록시도 OpenAI 스타일이라 가정
    text = data["choices"][0]["message"]["content"].strip()
    return text


def _generate_rule_based(event: HighlightEvent) -> str:
    """
    랜덤 템플릿 기반 코멘터리!!!!
    """
    t = event.highlightType
    me = event.nickname
    target = event.targetNickname

    if t == "OVERTAKE" and target:
        templates = [
            f"🔥 {me}님이 {target}님을 가볍게 추월했습니다!",
            f"와우!! {me}님, {target}님을 제치고 앞으로 나갑니다!!!",
            f"역전 성공!! {me}님이 {target}님을 추월했어요!!!",
            f"{target}님 앞을 스치듯 지나가는 {me}님!!!",
        ]
        return random.choice(templates)

    if t == "FINISH":
        templates = [
            f"🏁 {me}님, 멋진 완주입니다!! 축하드려요!!!",
            f"{me}님이 결승선을 통과했습니다!!! 대단해요!!",
            f"완주 성공!! {me}님 오늘 레전드 찍었습니다!!!",
        ]
        return random.choice(templates)

    if t == "ALMOST_FINISH":
        templates = [
            f"{me}님, 결승선이 바로 앞입니다!! 마지막 스퍼트!!!",
            f"이제 거의 다 왔어요 {me}님!! 조금만 더!!!",
        ]
        return random.choice(templates)

    if t == "TOP3_ENTRY":
        templates = [
            f"{me}님이 TOP3 안으로 진입했습니다!!!",
            f"{me}님, 드디어 상위권 입성!!! TOP3에 들어왔어요!!!",
        ]
        return random.choice(templates)

    if t == "SLOW_DOWN":
        templates = [
            f"{me}님, 잠깐 페이스 조절 중입니다! 숨 한 번 고르고 가요!!",
            f"{me}님, 조금만 더 힘내요!! 다시 올려봅시다!!!",
        ]
        return random.choice(templates)

    # 기타
    return f"{event.nickname}님의 레이스가 계속 이어지고 있습니다!!!"