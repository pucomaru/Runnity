package com.runnity.websocket.dto.websocket.server;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * USER_LEFT 메시지 (서버 → 클라이언트)
 * 
 * 다른 참가자가 챌린지에서 퇴장했을 때 전송되는 메시지입니다.
 * 프론트: 참가자 목록에서 제거
 * 
 * @param type 메시지 타입 ("USER_LEFT")
 * @param userId 퇴장한 사용자 ID
 * @param reason 퇴장 사유 (QUIT, FINISH, TIMEOUT, DISCONNECTED, KICKED, EXPIRED, ERROR)
 * @param timestamp 타임스탬프
 */
public record UserLeftMessage(
    @NotNull String type,
    @NotNull Long userId,
    @NotNull String reason,
    @NotNull Long timestamp
) {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    public UserLeftMessage {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        Set<ConstraintViolation<UserLeftMessage>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }
    }
    
    public UserLeftMessage(Long userId, String reason) {
        this("USER_LEFT", userId, reason, System.currentTimeMillis());
    }
}

