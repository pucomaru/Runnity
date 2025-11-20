import csv
import time

import requests

from config import BASE_URL, CHALLENGE_ID


LOGIN_URL = f"{BASE_URL}/api/v1/auth/test-login"
JOIN_URL = f"{BASE_URL}/api/v1/challenges/{CHALLENGE_ID}/join"


def test_login(member_id: int) -> str:
    """
    테스트 로그인으로 accessToken 발급
    """
    resp = requests.post(LOGIN_URL, params={"memberId": member_id}, timeout=5)
    resp.raise_for_status()
    data = resp.json()["data"]
    return data["accessToken"]


def join_challenge(member_id: int, challenge_id: int) -> None:
    """
    단일 member에 대해 챌린지 참가(join) 호출
    """
    access_token = test_login(member_id)
    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json",
    }

    url = f"{BASE_URL}/api/v1/challenges/{challenge_id}/join"
    resp = requests.post(url, json={}, headers=headers, timeout=5)

    if resp.status_code != 201:
        print(f"[WARN] member {member_id} join 실패: {resp.status_code} {resp.text}")
        return

    data = resp.json()["data"]
    print(
        f"[OK] member {member_id} 참가 완료 → "
        f"participantId={data['participantId']} status={data['status']}"
    )


def bulk_join_from_csv(csv_path: str):
    """
    CSV 파일을 읽어 여러 member를 한 번에 join
    """
    print(f"CSV 읽는 중: {csv_path}")

    with open(csv_path, newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            member_id = int(row["memberId"])
            # row["challengeId"]는 현재 CHALLENGE_ID와 동일하다고 가정
            try:
                join_challenge(member_id, CHALLENGE_ID)
                # 너무 빠르게 때리지 않도록 약간 딜레이
                time.sleep(0.05)
            except Exception as e:
                print(f"[ERROR] member {member_id} 처리 중 예외: {e}")


if __name__ == "__main__":
    # 한 파일만 돌리고 싶으면 range 혹은 path를 수정해서 사용
    for i in range(1, 11):
        path = f"test_members_{i}.csv"
        bulk_join_from_csv(path)


