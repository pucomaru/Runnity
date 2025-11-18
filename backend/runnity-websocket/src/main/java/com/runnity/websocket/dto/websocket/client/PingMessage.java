package com.runnity.websocket.dto.websocket.client;

/**
 * PING 메시지 (클라이언트 → 서버)
 * 
 * 연결 상태를 확인하기 위해 주기적으로 전송되는 메시지입니다 (예: 30초마다).
 * 서버는 PONG으로 응답합니다.
 * 
 * @param type 메시지 타입 ("PING")
 * @param timestamp 타임스탬프
 */
public record PingMessage(
    String type,
    Long timestamp
) {
    public PingMessage {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type은 필수입니다");
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }
    
    public PingMessage() {
        this("PING", System.currentTimeMillis());
    }
}

