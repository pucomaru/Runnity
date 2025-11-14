from pydantic import BaseModel
from typing import Optional

class HighlightEvent(BaseModel):
    challengeId: int
    runnerId: int
    nickname: str
    profileImage: Optional[str] = None

    # 하이라이트 종류
    highlightType: str              # OVERTAKE, FINISH, ALMOST_FINISH, TOP3_ENTRY, SLOW_DOWN

    targetNickname: Optional[str] = None
    rank: Optional[int] = None
    distance: Optional[float] = None
    totalDistance: Optional[float] = None
    pace: Optional[float] = None

    commentary: Optional[str] = None   # LLM 결과가 여기에 들어감
    timestamp: Optional[float] = None  # 나중에 클라이언트 싱크용 타임스탬프