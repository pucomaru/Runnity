package com.runnity.websocket.dto.redis;

/**
 * PARTICIPANT_UPDATE 이벤트 (Redis Pub/Sub)
 * 
 * WebSocket 서버 간 참가자 정보 업데이트 이벤트를 동기화합니다.
 * 채널: challenge:update
 * 
 * @param challengeId 챌린지 ID
 * @param userId 업데이트된 사용자 ID
 * @param distance 현재 거리 (km)
 * @param pace 현재 페이스 (분/km)
 * @param timestamp 이벤트 발생 시각
 */
public record ParticipantUpdateEvent(
    Long challengeId,
    Long userId,
    Double distance,
    Integer pace,
    Long timestamp
) {
    public ParticipantUpdateEvent {
        if (challengeId == null || challengeId <= 0) {
            throw new IllegalArgumentException("challengeId는 양수여야 합니다");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다");
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
    
    public ParticipantUpdateEvent(Long challengeId, Long userId, Double distance, Integer pace) {
        this(challengeId, userId, distance, pace, System.currentTimeMillis());
    }
}

