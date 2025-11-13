package com.runnity.websocket.dto.websocket.server;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * ERROR 메시지 (서버 → 클라이언트)
 * 
 * 클라이언트 메시지 처리 중 오류 발생 시 전송되는 메시지입니다.
 * 
 * @param type 메시지 타입 ("ERROR")
 * @param errorCode 오류 코드 (INVALID_MESSAGE, TIMEOUT, UNAUTHORIZED 등)
 * @param errorMessage 오류 메시지
 * @param timestamp 타임스탬프
 */
public record ErrorMessage(
    @NotNull String type,
    @NotNull String errorCode,
    @NotNull String errorMessage,
    @NotNull Long timestamp
) {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    public ErrorMessage {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        Set<ConstraintViolation<ErrorMessage>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }
    }
    
    public ErrorMessage(String errorCode, String errorMessage) {
        this("ERROR", errorCode, errorMessage, System.currentTimeMillis());
    }
}

