package com.runnity.websocket.dto.websocket.client;

/**
 * RECORD 메시지 (클라이언트 → 서버)
 * 
 * 주기적으로 전송되는 러닝 기록 메시지입니다 (예: 5초마다).
 * 서버는 이를 받아 Kafka로 발행 (eventType: running)합니다.
 * challengeId, userId는 세션에서 자동 추출됩니다.
 * 
 * @param type 메시지 타입 ("RECORD")
 * @param distance 누적 거리 (km)
 * @param pace 현재 페이스 (분/km)
 * @param timestamp 타임스탬프
 */
public record RecordMessage(
    String type,
    Double distance,
    Integer pace,
    Long timestamp
) {
    public RecordMessage {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type은 필수입니다");
        }
        if (distance == null || distance < 0) {
            throw new IllegalArgumentException("distance는 0 이상이어야 합니다");
        }
        if (pace == null || pace < 0) {
            throw new IllegalArgumentException("pace는 0 이상이어야 합니다");
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }
}

