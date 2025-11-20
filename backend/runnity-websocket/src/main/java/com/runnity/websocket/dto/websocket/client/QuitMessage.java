package com.runnity.websocket.dto.websocket.client;

/**
 * QUIT 메시지 (클라이언트 → 서버)
 * 
 * 사용자가 챌린지를 자발적으로 포기할 때 전송되는 메시지입니다.
 * 서버는 Kafka 발행 (eventType: leave, reason: QUIT)합니다.
 * 
 * @param type 메시지 타입 ("QUIT")
 * @param timestamp 타임스탬프
 */
public record QuitMessage(
    String type,
    Long timestamp
) {
    public QuitMessage {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type은 필수입니다");
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }
    
    public QuitMessage() {
        this("QUIT", System.currentTimeMillis());
    }
}

