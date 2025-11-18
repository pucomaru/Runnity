package com.runnity.websocket.dto.websocket.client;

/**
 * PONG 메시지 (클라이언트 → 서버 또는 서버 → 클라이언트)
 * 
 * 서버의 PING에 대한 응답 메시지입니다.
 * 
 * @param type 메시지 타입 ("PONG")
 * @param timestamp 타임스탬프
 */
public record PongMessage(
    String type,
    Long timestamp
) {
    public PongMessage {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type은 필수입니다");
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }
    
    public PongMessage() {
        this("PONG", System.currentTimeMillis());
    }
}

