package com.runnity.websocket.dto.websocket.client;

/**
 * KICKED 메시지 (클라이언트 → 서버)
 * 
 * 이상 사용자가 자신의 웹소켓 연결에서 강제 퇴장 메시지를 받을 때 전송되는 메시지입니다.
 * 서버는 Kafka 발행 (eventType: leave, reason: KICKED)합니다.
 * 
 * @param type 메시지 타입 ("KICKED")
 * @param timestamp 타임스탬프
 */
public record KickedMessage(
    String type,
    Long timestamp
) {
    public KickedMessage {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type은 필수입니다");
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }
    
    public KickedMessage() {
        this("KICKED", System.currentTimeMillis());
    }
}

