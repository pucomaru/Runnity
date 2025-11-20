from pydantic import BaseModel
from typing import Optional

# # Stream → LLM
# “하이라이트가 발생했다.
# 추월/Top3/완주 등의 이벤트가 발생했다.
# 이걸 기반으로 멘트를 만들어달라.”

class HighlightEvent(BaseModel):
    challengeId: int
    runnerId: int
    nickname: str
    profileImage: Optional[str] = None

    # 하이라이트 종류
    highlightType: str              # OVERTAKE, FINISH, ALMOST_FINISH, TOP3_ENTRY, SLOW_DOWN

    targetRunnerId: Optional[int] = None    # 추가
    targetNickname: Optional[str] = None
    targetProfileImage: Optional[str] = None  # 추가
    rank: Optional[int] = None
    distance: Optional[float] = None
    totalDistance: Optional[float] = None
    pace: Optional[float] = None

    commentary: Optional[str] = None   # LLM 결과가 여기에 들어감
    timestamp: Optional[float] = None  # 나중에 클라이언트 싱크용 타임스탬프