package com.runnity.websocket.dto.websocket.client;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * KICKED 메시지 (클라이언트 → 서버)
 * 
 * 이상 사용자가 자신의 웹소켓 연결에서 강제 퇴장 메시지를 받을 때 전송되는 메시지입니다.
 * 서버는 Kafka 발행 (eventType: leave, reason: KICKED)합니다.
 * 
 * @param type 메시지 타입 ("KICKED")
 * @param timestamp 타임스탬프
 */
public record KickedMessage(
    @NotNull String type,
    @NotNull Long timestamp
) {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    public KickedMessage {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        Set<ConstraintViolation<KickedMessage>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }
    }
    
    public KickedMessage() {
        this("KICKED", System.currentTimeMillis());
    }
}

