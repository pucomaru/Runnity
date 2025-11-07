package com.runnity.challenge.domain;

import java.util.Arrays;
import java.util.Optional;

public enum ChallengePhase {

    RECRUITING("RECRUITING", "모집 중"),
    ALL("ALL", "전체");

    private final String code;
    private final String label;

    ChallengePhase(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static Optional<ChallengePhase> fromCode(String code) {
        return Arrays.stream(values())
                .filter(phase -> phase.code.equalsIgnoreCase(code))
                .findFirst();
    }

    @Override
    public String toString() {
        return label + " (" + code + ")";
    }
}