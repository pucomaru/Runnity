package com.runnity.websocket.dto.redis;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

/**
 * USER_LEFT 이벤트 (Redis Pub/Sub)
 * 
 * WebSocket 서버 간 사용자 퇴장 이벤트를 동기화합니다.
 * 채널: challenge:leave
 * 
 * @param type 이벤트 타입 ("USER_LEFT")
 * @param challengeId 챌린지 ID
 * @param userId 퇴장한 사용자 ID
 * @param reason 퇴장 사유 (QUIT, FINISH, TIMEOUT, DISCONNECTED, KICKED, EXPIRED, ERROR)
 * @param timestamp 타임스탬프
 */
public record UserLeftEvent(
    @NotNull String type,
    @NotNull @Positive Long challengeId,
    @NotNull Long userId,
    @NotNull String reason,
    @NotNull Long timestamp
) {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    public UserLeftEvent {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        Set<ConstraintViolation<UserLeftEvent>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }
    }
    
    public UserLeftEvent(Long challengeId, Long userId, String reason) {
        this("USER_LEFT", challengeId, userId, reason, System.currentTimeMillis());
    }
}

