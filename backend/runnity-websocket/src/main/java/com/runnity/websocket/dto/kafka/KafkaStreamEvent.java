package com.runnity.websocket.dto.kafka;

import com.runnity.websocket.enums.KafkaEventType;

/**
 * Kafka 스트림 이벤트
 * 
 * Stream 서버로 전송되는 브로드캐스트 이벤트
 * 
 * @param eventType 이벤트 타입 (START, RUNNING, FINISH, LEAVE)
 * @param challengeId 챌린지 ID
 * @param runnerId 러너 ID (Member PK)
 * @param nickname 러너 닉네임
 * @param profileImage 프로필 이미지 URL
 * @param distance 현재 누적 거리 (km)
 * @param pace 현재 페이스 (분/km)
 * @param ranking 현재 순위
 * @param isBroadcast 브로드캐스트 여부
 * @param reason 퇴장 사유 (leave일 때만 사용)
 * @param timestamp 이벤트 발생 시각
 */
public record KafkaStreamEvent(
    KafkaEventType eventType,
    Long challengeId,
    Long runnerId,
    String nickname,
    String profileImage,
    Double distance,
    Integer pace,
    Integer ranking,
    Boolean isBroadcast,
    String reason,
    Long timestamp
) {
    public KafkaStreamEvent {
        if (eventType == null) {
            throw new IllegalArgumentException("eventType은 필수입니다");
        }
        if (challengeId == null || challengeId <= 0) {
            throw new IllegalArgumentException("challengeId는 양수여야 합니다");
        }
        if (runnerId == null || runnerId <= 0) {
            throw new IllegalArgumentException("runnerId는 양수여야 합니다");
        }
        if (distance != null && distance < 0) {
            throw new IllegalArgumentException("distance는 0 이상이어야 합니다");
        }
        if (pace != null && pace < 0) {
            throw new IllegalArgumentException("pace는 0 이상이어야 합니다");
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }
}

