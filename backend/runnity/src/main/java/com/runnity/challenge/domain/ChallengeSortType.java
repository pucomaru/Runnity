package com.runnity.challenge.domain;

import java.util.Arrays;
import java.util.Optional;

public enum ChallengeSortType {

    POPULAR("POPULAR", "인기순 (참가자 수 기준)"),
    LATEST("LATEST", "최신순 (생성일 기준)");

    private final String code;
    private final String label;

    ChallengeSortType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static Optional<ChallengeSortType> fromCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equalsIgnoreCase(code))
                .findFirst();
    }

    @Override
    public String toString() {
        return label + " (" + code + ")";
    }
}