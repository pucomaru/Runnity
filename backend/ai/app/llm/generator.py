# 멘트 생성 로직
def generate_commentary(event):
    t = event.get("highlightType")
    nickname = event.get("nickname")
    target = event.get("targetNickname")

    if t == "OVERTAKE":
        return f"{nickname}님이 {target}님을 역전했습니다!"
    elif t == "FINISH":
        return f"{nickname}님이 완주했습니다! 축하드립니다!"
    elif t == "ALMOST_FINISH":
        return f"{nickname}님, 이제 곧 결승선이 보입니다!"
    elif t == "TOP3_ENTRY":
        return f"{nickname}님이 TOP 3에 진입했습니다!"
    elif t == "SLOW_DOWN":
        return f"{nickname}님, 속도가 다소 떨어지고 있습니다!"
    else:
        return f"{nickname}님의 주행이 계속되고 있습니다."