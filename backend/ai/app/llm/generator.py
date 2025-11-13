import os
import random
import requests
from app.models.highlight_event import HighlightEvent
from app.utils.logger import logger

GMS_API_KEY = os.getenv("GMS_API_KEY")
GMS_CHAT_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions"

# 멘트 생성 로직(공통)
def generate_commentary(event: HighlightEvent) -> str:
    if GMS_API_KEY:
        try:
            llm_text = _generate_with_llm(event)
            if llm_text:
                logger.info(f"[LLM COMMENTARY] {llm_text}")
                return llm_text
        except Exception as e:
            logger.error(f"[LLM ERROR] {e}")

    # fallback
    rule_text = _generate_rule_based(event)
    logger.info(f"[RULE COMMENTARY] {rule_text}")
    return rule_text


# ============================
# 1) LLM 기반 멘트 생성
# ============================
def _generate_with_llm(event: HighlightEvent) -> str:
    nickname = event.nickname
    t = event.highlightType
    target = event.targetNickname or "상대"
    rank = event.rank
    dist = event.totalDistance
    pace = event.pace
    timestamp = event.timestamp

    # GMS 호환 이슈 때문에 role=developer 사용
    system_msg = {
        "role": "developer",
        "content": (
            "너는 MZ세대 스타일 중계자이며, "
            "위트 있고 에너지 넘치면서도 프로페셔널한 AI 중계 캐스터다."
        )
    }

    prompt_text = f"""
[이벤트 정보]
- 이벤트 유형: {t}
- 주인공: {nickname}
- 대상자: {target}
- 현재 순위: {rank}
- 누적 거리: {dist} km
- 현재 페이스: {pace} 분/km
- 발생 시각: {timestamp}

[요구사항]
1) 한국어 한 줄만 생성
2) 20~45자
3) MZ 스타일 + 밈 약간
4) 문장 끝 느낌표 또는 마침표
5) 중계 톤으로 자연스럽게
"""

    payload = {
        "model": "gpt-5-mini",
        "messages": [
            system_msg,
            {"role": "user", "content": prompt_text.strip()},
        ],
        "max_tokens": 80,
        "temperature": 1.1,
    }

    headers = {
        "Authorization": f"Bearer {GMS_API_KEY}",
        "Content-Type": "application/json",
    }

    resp = requests.post(GMS_CHAT_URL, json=payload, headers=headers, timeout=10)

    # GMS는 정상 응답이 아닐 때 choices가 없음 → 반드시 체크 필요
    data = resp.json()
    if "choices" not in data:
        raise Exception(f"GMS Error Response: {data}")

    return data["choices"][0]["message"]["content"].strip()

# ============================
# 2) Fallback (룰 기반)
# ============================
def _generate_rule_based(event: HighlightEvent) -> str:
    t = event.highlightType
    me = event.nickname
    target = event.targetNickname

    if t == "OVERTAKE" and target:
        templates = [
            f"{me}님이 {target}님을 가볍게 추월했습니다!",
            f"와우!! {me}님, {target}님을 제치고 앞으로 나갑니다!!!",
            f"역전 성공!! {me}님이 {target}님을 추월했어요!!!",
            f"{target}님 앞을 스치듯 지나가는 {me}님!!!",
        ]
        return random.choice(templates)

    if t == "FINISH":
        templates = [
            f"{me}님, 멋진 완주입니다!! 축하드려요!!!",
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
            f"{me}님, 잠깐 페이스 조절 중입니다! 숨 한번 고르고 가요!!",
            f"{me}님, 조금만 더 힘내요!! 다시 올려봅시다!!!",
        ]
        return random.choice(templates)

    return f"{event.nickname}님의 레이스가 계속 이어지고 있습니다!!!"