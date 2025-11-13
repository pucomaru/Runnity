package com.runnity.websocket.dto.websocket.client;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * PING 메시지 (클라이언트 → 서버)
 * 
 * 연결 상태를 확인하기 위해 주기적으로 전송되는 메시지입니다 (예: 30초마다).
 * 서버는 PONG으로 응답합니다.
 * 
 * @param type 메시지 타입 ("PING")
 * @param timestamp 타임스탬프
 */
public record PingMessage(
    @NotNull String type,
    @NotNull Long timestamp
) {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    public PingMessage {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        Set<ConstraintViolation<PingMessage>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }
    }
    
    public PingMessage() {
        this("PING", System.currentTimeMillis());
    }
}

