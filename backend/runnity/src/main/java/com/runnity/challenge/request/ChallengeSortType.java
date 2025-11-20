package com.runnity.challenge.request;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;

public enum ChallengeSortType {

    POPULAR("POPULAR", "인기순 (참가자 수 기준)"),
    LATEST("LATEST", "임박순 (빨리 시작하는 순)");

    private final String code;
    private final String label;

    ChallengeSortType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    @JsonValue
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
}