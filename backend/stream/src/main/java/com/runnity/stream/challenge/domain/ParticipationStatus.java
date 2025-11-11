package com.runnity.stream.challenge.domain;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;

public enum ParticipationStatus {

    WAITING("WAITING", "대기 중"),
    RUNNING("RUNNING", "달리는 중"),
    COMPLETED("COMPLETED", "완주"),
    QUIT("QUIT", "자발적 포기"),
    TIMEOUT("TIMEOUT", "무응답으로 종료"),
    DISCONNECTED("DISCONNECTED", "연결 끊김"),
    KICKED("KICKED", "강제 퇴장"),
    ERROR("ERROR", "서버 오류 등 비정상 종료"),
    EXPIRED("EXPIRED", "종료 시간 도달"),
    NOT_STARTED("NOT_STARTED", "참여 안함"),
    LEFT("LEFT", "참여 취소");

    private final String code;
    private final String label;

    ParticipationStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static Optional<ParticipationStatus> fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equalsIgnoreCase(code))
                .findFirst();
    }

    /**
     * 챌린지가 아직 진행 중인 상태인지 확인
     */
    public boolean isRejoinable() {
        return REJOINABLE_STATUSES.contains(this);
    }

    public boolean isTimeOverlapRelevant() {
        return TIME_OVERLAP_STATUSES.contains(this);
    }

    public static final EnumSet<ParticipationStatus> REJOINABLE_STATUSES =
            EnumSet.of(TIMEOUT, DISCONNECTED, ERROR);

    public static final EnumSet<ParticipationStatus> TIME_OVERLAP_STATUSES =
            EnumSet.of(WAITING, RUNNING, TIMEOUT, DISCONNECTED, ERROR);

    public static final EnumSet<ParticipationStatus> ACTIVE_PARTICIPATION_STATUSES =
            EnumSet.of(WAITING, RUNNING, COMPLETED, QUIT, TIMEOUT, DISCONNECTED, KICKED, ERROR, EXPIRED, NOT_STARTED);

    public static final EnumSet<ParticipationStatus> CHALLENGE_START_STATUS =
            EnumSet.of(WAITING);

    public static final EnumSet<ParticipationStatus> CHALLENGE_END_STATUS =
            EnumSet.of(COMPLETED);

    @Override
    public String toString() {
        return label + " (" + code + ")";
    }
}

