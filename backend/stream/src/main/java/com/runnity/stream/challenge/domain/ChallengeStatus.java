package com.runnity.stream.challenge.domain;

import java.util.Arrays;
import java.util.Optional;

public enum ChallengeStatus {

    RECRUITING("RECRUITING", "모집중"),
    READY("READY", "준비완료"),
    RUNNING("RUNNING", "진행중"),
    DONE("DONE", "종료");

    private final String code;
    private final String label;

    ChallengeStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static Optional<ChallengeStatus> fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equalsIgnoreCase(code))
                .findFirst();
    }

    @Override
    public String toString() {
        return label + " (" + code + ")";
    }
}


