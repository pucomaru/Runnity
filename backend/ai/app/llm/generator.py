import os
import random
import requests
from app.models.highlight_event import HighlightEvent
from app.utils.logger import logger

# ============================================
# ENV & URL
# ============================================
GMS_API_KEY = os.getenv("GMS_API_KEY")
GMS_CHAT_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions"


# ============================================
# MAIN ENTRY
# ============================================
def generate_commentary(event: HighlightEvent) -> str:
    """
    최종 코멘터리 생성 함수
    1) LLM 우선
    2) 실패 시 rule-based fallback
    """
    if GMS_API_KEY:
        try:
            llm_text = _generate_with_llm(event)
            if llm_text:
                logger.info(f"[LLM COMMENTARY] {llm_text}")
                return llm_text
            else:
                logger.warning(f"[LLM EMPTY] highlightType={event.highlightType} nickname={event.nickname}")
        except Exception as e:
            logger.error(f"[LLM ERROR] {e}")

    # fallback
    rule_text = _generate_rule_based(event)
    logger.info(f"[RULE COMMENTARY] {rule_text}")
    return rule_text


# ============================================
# 1) GMS LLM 기반 코멘터리 생성
# ============================================
def _generate_with_llm(event: HighlightEvent) -> str:
    nickname = event.nickname
    t = event.highlightType
    target = event.targetNickname or "상대"
    rank = event.rank
    dist = event.totalDistance
    pace = event.pace
    timestamp = event.timestamp

    # System Prompt (GMS는 role=developer 쓰는게 안전)
    system_msg = {
        "role": "developer",
        "content": (
            "너는 MZ세대 스타일 중계자이며, "
            "위트 있고 에너지 넘치면서도 프로페셔널한 AI 중계 캐스터다."
        )
    }

    user_prompt = f"""
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
4) 문장 끝은 느낌표 또는 마침표
5) 중계 톤으로 자연스럽고 현장감 있게
"""

    payload = {
        "model": "gpt-4o",
        "messages": [
            system_msg,
            {"role": "user", "content": user_prompt.strip()},
        ],

        # 🔥 max_tokens → GMS는 지원 안 하고 max_completion_tokens 써야 함
        "max_completion_tokens": 80,

        # 🔥 GMS에서 temperature 1만 지원함
        "temperature": 1,
    }

    headers = {
        "Authorization": f"Bearer {GMS_API_KEY}",
        "Content-Type": "application/json",
    }

    resp = requests.post(GMS_CHAT_URL, json=payload, headers=headers, timeout=15)

    if not resp.ok:
        raise Exception(f"GMS HTTP {resp.status_code}: {resp.text}")

    try:
        data = resp.json()
    except Exception:
        raise Exception(f"GMS Invalid JSON: {resp.text}")

    # 🔥 핵심: GMS 응답 구조 완벽 파싱
    text = _extract_llm_text(data)

    if not text:
        raise Exception(f"GMS returned empty or invalid content: {data}")

    return text


# ============================================
# 1-1) GMS 응답 파서(핵심)
# ============================================
def _extract_llm_text(data):
    """
    GMS는 OpenAI와 완벽히 동일한 구조가 아님.
    상황에 따라 message/content, text, delta/content 등 다양하게 변함.
    → 모든 경우를 안전하게 처리
    """

    choices = data.get("choices", [])
    if not choices:
        return None

    choice = choices[0]

    # 1) 최신 OpenAI 구조
    try:
        text = choice["message"]["content"]
        if text and text.strip():
            return text.strip()
    except:
        pass

    # 2) OpenAI 구버전 구조
    try:
        text = choice["text"]
        if text and text.strip():
            return text.strip()
    except:
        pass

    # 3) delta 구조
    try:
        text = choice["delta"]["content"]
        if text and text.strip():
            return text.strip()
    except:
        pass

    # 4) 아무것도 없음
    return None


# ============================================
# 2) Rule-based fallback
# ============================================
def _generate_rule_based(event: HighlightEvent) -> str:
    t = event.highlightType
    me = event.nickname
    target = event.targetNickname

    if t == "OVERTAKE" and target:
        return random.choice([
            f"{me}님이 {target}님을 가볍게 추월했습니다!",
            f"와우!! {me}님, {target}님을 제치고 앞으로 나갑니다!!!",
            f"역전 성공!! {me}님이 {target}님을 추월했어요!!!",
            f"{target}님 앞을 스치듯 지나가는 {me}님!!!",
        ])

    if t == "FINISH":
        return random.choice([
            f"{me}님, 멋진 완주입니다!! 축하드려요!!!",
            f"{me}님이 결승선을 통과했습니다!!! 대단해요!!",
            f"완주 성공!! {me}님 오늘 레전드 찍었습니다!!!",
        ])

    if t == "ALMOST_FINISH":
        return random.choice([
            f"{me}님, 결승선이 바로 앞입니다!! 마지막 스퍼트!!!",
            f"이제 거의 다 왔어요 {me}님!! 조금만 더!!!",
        ])

    if t == "TOP3_ENTRY":
        return random.choice([
            f"{me}님이 TOP3 안으로 진입했습니다!!!",
            f"{me}님, 드디어 상위권 입성!!! TOP3에 들어왔어요!!!",
        ])

    if t == "SLOW_DOWN":
        return random.choice([
            f"{me}님, 페이스 조절 중입니다! 숨 한번 고르고 가요!!",
            f"{me}님, 조금만 더 힘내요!! 다시 올려봅시다!!!",
        ])

    return f"{event.nickname}님의 레이스가 계속 이어지고 있습니다!!!"
