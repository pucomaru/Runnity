package com.runnity.history.domain;

import java.util.Arrays;
import java.util.Optional;

public enum  RunRecordType {
    PERSONAL("PERSONAL", "개인 러닝"),
    CHALLENGE("CHALLENGE", "챌린지");

    private final String code;
    private final String label;

    RunRecordType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() { return code; }

    public String label() { return label; }

    public static Optional<RunRecordType> fromCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equalsIgnoreCase(code))
                .findFirst();
    }

    @Override
    public String toString() { return label + " (" + code + ")"; }

}
