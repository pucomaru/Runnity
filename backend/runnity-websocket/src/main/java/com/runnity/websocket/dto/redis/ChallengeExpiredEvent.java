package com.runnity.websocket.dto.redis;

/**
 * CHALLENGE_EXPIRED 이벤트 (Redis Pub/Sub)
 * 
 * 비즈니스 서버에서 챌린지 종료 처리 후 발행하는 이벤트
 * (비즈니스 서버의 handleDone()에서 challenge:*:done 처리 후 발행)
 * 채널: challenge:expired
 * 
 * @param challengeId 챌린지 ID
 * @param timestamp 이벤트 발생 시각
 */
public record ChallengeExpiredEvent(
    Long challengeId,
    Long timestamp
) {
    public ChallengeExpiredEvent {
        if (challengeId == null || challengeId <= 0) {
            throw new IllegalArgumentException("challengeId는 양수여야 합니다");
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }
    
    public ChallengeExpiredEvent(Long challengeId) {
        this(challengeId, System.currentTimeMillis());
    }
}

