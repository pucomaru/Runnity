package com.runnity.websocket.dto.websocket.client;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Set;

/**
 * RECORD 메시지 (클라이언트 → 서버)
 * 
 * 주기적으로 전송되는 러닝 기록 메시지입니다 (예: 5초마다).
 * 서버는 이를 받아 Kafka로 발행 (eventType: running)합니다.
 * challengeId, userId는 세션에서 자동 추출됩니다.
 * 
 * @param type 메시지 타입 ("RECORD")
 * @param distance 누적 거리 (km)
 * @param pace 현재 페이스 (분/km)
 * @param timestamp 타임스탬프
 */
public record RecordMessage(
    @NotNull String type,
    @NotNull @PositiveOrZero Double distance,
    @NotNull @PositiveOrZero Double pace,
    @NotNull Long timestamp
) {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    public RecordMessage {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        Set<ConstraintViolation<RecordMessage>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }
    }
}

