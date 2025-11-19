package com.runnity.stream.scheduler.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationTemplate {
    CHALLENGE_READY("[Runnity] 챌린지 시작", "챌린지 시작 5분 전입니다. 곧 입장 가능합니다!"),
    CHALLENGE_DONE("[Runnity] 챌린지 종료", "챌린지가 종료되었습니다. 수고하셨습니다!");

    private final String title;
    private final String body;
}
