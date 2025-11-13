package com.runnity.websocket.dto.websocket.server;

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
    String type,
    String errorCode,
    String errorMessage,
    Long timestamp
) {
    public ErrorMessage {
        if (errorCode == null || errorCode.isBlank()) {
            throw new IllegalArgumentException("errorCode는 필수입니다");
        }
        if (errorMessage == null || errorMessage.isBlank()) {
            throw new IllegalArgumentException("errorMessage는 필수입니다");
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }
    
    public ErrorMessage(String errorCode, String errorMessage) {
        this("ERROR", errorCode, errorMessage, System.currentTimeMillis());
    }
}
