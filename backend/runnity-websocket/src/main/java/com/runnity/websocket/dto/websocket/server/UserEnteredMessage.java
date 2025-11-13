package com.runnity.websocket.dto.websocket.server;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Set;

/**
 * USER_ENTERED 메시지 (서버 → 클라이언트)
 * 
 * 다른 참가자가 챌린지에 입장했을 때 전송되는 메시지입니다.
 * 프론트: 참가자 목록에 추가
 * 
 * @param type 메시지 타입 ("USER_ENTERED")
 * @param userId 입장한 사용자 ID
 * @param nickname 닉네임
 * @param profileImage 프로필 이미지 URL
 * @param distance 초기 거리 (0.0)
 * @param pace 초기 페이스 (0.0)
 * @param timestamp 타임스탬프
 */
public record UserEnteredMessage(
    @NotNull String type,
    @NotNull Long userId,
    @NotNull String nickname,
    String profileImage,
    @NotNull @PositiveOrZero Double distance,
    @NotNull @PositiveOrZero Double pace,
    @NotNull Long timestamp
) {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    public UserEnteredMessage {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        Set<ConstraintViolation<UserEnteredMessage>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }
    }
    
    public UserEnteredMessage(Long userId, String nickname, String profileImage, Double distance, Double pace) {
        this("USER_ENTERED", userId, nickname, profileImage, distance, pace, System.currentTimeMillis());
    }
}

