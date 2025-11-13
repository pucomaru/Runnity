package com.runnity.websocket.dto.websocket.server;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Set;

/**
 * PARTICIPANT_UPDATE 메시지 (서버 → 클라이언트)
 * 
 * 다른 참가자의 distance, pace가 업데이트될 때 전송되는 메시지입니다.
 * 프론트: 해당 참가자 정보만 업데이트
 * 
 * @param type 메시지 타입 ("PARTICIPANT_UPDATE")
 * @param userId 업데이트된 사용자 ID
 * @param distance 현재 거리 (km)
 * @param pace 현재 페이스 (분/km)
 * @param timestamp 타임스탬프
 */
public record ParticipantUpdateMessage(
    @NotNull String type,
    @NotNull Long userId,
    @NotNull @PositiveOrZero Double distance,
    @NotNull @PositiveOrZero Double pace,
    @NotNull Long timestamp
) {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    public ParticipantUpdateMessage {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        Set<ConstraintViolation<ParticipantUpdateMessage>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }
    }
    
    public ParticipantUpdateMessage(Long userId, Double distance, Double pace) {
        this("PARTICIPANT_UPDATE", userId, distance, pace, System.currentTimeMillis());
    }
}

