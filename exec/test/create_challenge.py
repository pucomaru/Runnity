import requests
from datetime import datetime, timedelta

from config import BASE_URL, ADMIN_MEMBER_ID


def test_login(member_id: int) -> str:
    """
    테스트 로그인 API를 통해 accessToken 발급
    """
    resp = requests.post(
        f"{BASE_URL}/api/v1/auth/test-login",
        params={"memberId": member_id},
        timeout=5,
    )
    resp.raise_for_status()
    data = resp.json()["data"]
    return data["accessToken"]


def create_admin_challenge(access_token: str) -> int:
    """
    관리자 권한으로 챌린지 생성
    - startAt은 현재 시각 기준으로 10분 뒤로 설정
    """
    url = f"{BASE_URL}/api/v1/admin/challenges"
    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json",
    }

    # 지금 시각 기준으로 10분 후 (서버와 같은 타임존이라고 가정)
    start_at_dt = datetime.now() + timedelta(minutes=10)
    # 백엔드에서 사용 중인 포맷에 맞춰 초까지 표현 (예: 2025-11-21T10:00:00)
    start_at_str = start_at_dt.strftime("%Y-%m-%dT%H:%M:%S")

    body = {
        "title": "부하 테스트용 챌린지",
        "description": "WebSocket 부하 테스트를 위한 대규모 챌린지입니다.",
        "maxParticipants": 10000,
        "startAt": start_at_str,
        "distance": "FIVE",
        "isPrivate": False,
        "password": None,
        "isBroadcast": False,
    }

    print(f"요청 startAt: {start_at_str}")

    resp = requests.post(url, json=body, headers=headers, timeout=5)
    resp.raise_for_status()
    data = resp.json()["data"]
    print("챌린지 생성 완료:", data)
    return data["challengeId"]


if __name__ == "__main__":
    token = test_login(ADMIN_MEMBER_ID)
    challenge_id = create_admin_challenge(token)
    print("생성된 challengeId =", challenge_id)


