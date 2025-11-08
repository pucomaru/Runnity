package com.runnity.challenge.request;

import java.util.Arrays;
import java.util.Optional;

public enum ChallengeVisibility {

    PUBLIC("PUBLIC", "공개방만 조회"),
    ALL("ALL", "전체 조회 (공개 + 비공개)");

    private final String code;
    private final String label;

    ChallengeVisibility(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static Optional<ChallengeVisibility> fromCode(String code) {
        return Arrays.stream(values())
                .filter(visibility -> visibility.code.equalsIgnoreCase(code))
                .findFirst();
    }

    @Override
    public String toString() {
        return label + " (" + code + ")";
    }
}
