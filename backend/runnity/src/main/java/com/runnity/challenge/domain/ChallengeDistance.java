package com.runnity.challenge.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;

public enum ChallengeDistance {

    ONE("ONE", 1.0f, 20),
    TWO("TWO", 2.0f, 30),
    THREE("THREE", 3.0f, 45),
    FOUR("FOUR", 4.0f, 60),
    FIVE("FIVE", 5.0f, 60),
    SIX("SIX", 6.0f, 75),
    SEVEN("SEVEN", 7.0f, 90),
    EIGHT("EIGHT", 8.0f, 105),
    NINE("NINE", 9.0f, 120),
    TEN("TEN", 10.0f, 120),
    FIFTEEN("FIFTEEN", 15.0f, 135),
    HALF("HALF", 21.0975f, 180);

    private final String code;
    private final Float value;
    private final int durationMinutes;

    ChallengeDistance(String code, Float value, int durationMinutes) {
        this.code = code;
        this.value = value;
        this.durationMinutes = durationMinutes;
    }

    @JsonValue
    public String code() {
        return code;
    }

    public Float value() {
        return value;
    }

    public int durationMinutes() {
        return durationMinutes;
    }

    public static Optional<ChallengeDistance> fromValue(Float value) {
        return Arrays.stream(values())
                .filter(d -> Float.compare(d.value, value) == 0)
                .findFirst();
    }

    @Override
    public String toString() {
        return value + "km (" + code + ")";
    }
}
