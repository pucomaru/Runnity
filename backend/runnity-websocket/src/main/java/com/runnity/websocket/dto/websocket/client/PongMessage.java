package com.runnity.websocket.dto.websocket.client;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * PONG 메시지 (클라이언트 → 서버 또는 서버 → 클라이언트)
 * 
 * 서버의 PING에 대한 응답 메시지입니다.
 * 
 * @param type 메시지 타입 ("PONG")
 * @param timestamp 타임스탬프
 */
public record PongMessage(
    @NotNull String type,
    @NotNull Long timestamp
) {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    public PongMessage {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        Set<ConstraintViolation<PongMessage>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }
    }
    
    public PongMessage() {
        this("PONG", System.currentTimeMillis());
    }
}

