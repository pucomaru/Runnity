package com.runnity.websocket.dto.redis;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

/**
 * USER_ENTERED 이벤트 (Redis Pub/Sub)
 * 
 * WebSocket 서버 간 사용자 입장 이벤트를 동기화합니다.
 * 채널: challenge:enter
 * 
 * @param type 이벤트 타입 ("USER_ENTERED")
 * @param challengeId 챌린지 ID
 * @param userId 입장한 사용자 ID
 * @param nickname 닉네임
 * @param profileImage 프로필 이미지 URL
 * @param timestamp 타임스탬프
 */
public record UserEnteredEvent(
    @NotNull String type,
    @NotNull @Positive Long challengeId,
    @NotNull Long userId,
    @NotNull String nickname,
    String profileImage,
    @NotNull Long timestamp
) {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    public UserEnteredEvent {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        Set<ConstraintViolation<UserEnteredEvent>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }
    }
    
    public UserEnteredEvent(Long challengeId, Long userId, String nickname, String profileImage) {
        this("USER_ENTERED", challengeId, userId, nickname, profileImage, System.currentTimeMillis());
    }
}

