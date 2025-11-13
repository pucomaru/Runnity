package com.runnity.websocket.dto.redis;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Set;

/**
 * PARTICIPANT_UPDATE 이벤트 (Redis Pub/Sub)
 * 
 * WebSocket 서버 간 참가자 정보 업데이트 이벤트를 동기화합니다.
 * 채널: challenge:update
 * 
 * @param type 이벤트 타입 ("PARTICIPANT_UPDATE")
 * @param challengeId 챌린지 ID
 * @param userId 업데이트된 사용자 ID
 * @param distance 현재 거리 (km)
 * @param pace 현재 페이스 (분/km)
 * @param timestamp 타임스탬프
 */
public record ParticipantUpdateEvent(
    @NotNull String type,
    @NotNull @Positive Long challengeId,
    @NotNull Long userId,
    @NotNull @PositiveOrZero Double distance,
    @NotNull @PositiveOrZero Double pace,
    @NotNull Long timestamp
) {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    public ParticipantUpdateEvent {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        Set<ConstraintViolation<ParticipantUpdateEvent>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }
    }
    
    public ParticipantUpdateEvent(Long challengeId, Long userId, Double distance, Double pace) {
        this("PARTICIPANT_UPDATE", challengeId, userId, distance, pace, System.currentTimeMillis());
    }
}

