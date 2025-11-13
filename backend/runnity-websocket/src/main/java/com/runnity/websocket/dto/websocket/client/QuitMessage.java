package com.runnity.websocket.dto.websocket.client;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * QUIT 메시지 (클라이언트 → 서버)
 * 
 * 사용자가 챌린지를 자발적으로 포기할 때 전송되는 메시지입니다.
 * 서버는 Kafka 발행 (eventType: leave, reason: QUIT)합니다.
 * 
 * @param type 메시지 타입 ("QUIT")
 * @param timestamp 타임스탬프
 */
public record QuitMessage(
    @NotNull String type,
    @NotNull Long timestamp
) {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    public QuitMessage {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        Set<ConstraintViolation<QuitMessage>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }
    }
    
    public QuitMessage() {
        this("QUIT", System.currentTimeMillis());
    }
}

