package com.runnity.websocket.dto.redis;

/**
 * USER_ENTERED 이벤트 (Redis Pub/Sub)
 * 
 * WebSocket 서버 간 사용자 입장 이벤트를 동기화합니다.
 * 채널: challenge:enter
 * 
 * @param challengeId 챌린지 ID
 * @param userId 입장한 사용자 ID
 * @param nickname 닉네임
 * @param profileImage 프로필 이미지 URL
 * @param timestamp 이벤트 발생 시각
 */
public record UserEnteredEvent(
    Long challengeId,
    Long userId,
    String nickname,
    String profileImage,
    Long timestamp
) {
    public UserEnteredEvent {
        if (challengeId == null || challengeId <= 0) {
            throw new IllegalArgumentException("challengeId는 양수여야 합니다");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다");
        }
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("nickname은 필수입니다");
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }
    
    public UserEnteredEvent(Long challengeId, Long userId, String nickname, String profileImage) {
        this(challengeId, userId, nickname, profileImage, System.currentTimeMillis());
    }
}

