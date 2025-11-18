from pydantic import BaseModel
from typing import Optional
# “하이라이트 멘트를 만들어서 보냄.”
class LLMCommentaryMessage(BaseModel):
    challengeId: int
    runnerId: int
    nickname: str
    profileImage: Optional[str]

    highlightType: str
    commentary: str

    targetRunnerId: Optional[int] = None
    targetNickname: Optional[str] = None
    targetProfileImage: Optional[str] = None

    distance: Optional[float] = None
    pace: Optional[float] = None
    ranking: Optional[int] = None
