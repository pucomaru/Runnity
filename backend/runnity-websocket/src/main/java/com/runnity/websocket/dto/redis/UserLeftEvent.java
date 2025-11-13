package com.runnity.websocket.dto.redis;

/**
 * USER_LEFT 이벤트 (Redis Pub/Sub)
 * 
 * WebSocket 서버 간 사용자 퇴장 이벤트를 동기화합니다.
 * 채널: challenge:leave
 * 
 * @param challengeId 챌린지 ID
 * @param userId 퇴장한 사용자 ID
 * @param reason 퇴장 사유 (QUIT, FINISH, TIMEOUT, DISCONNECTED, KICKED, EXPIRED, ERROR)
 */
public record UserLeftEvent(
    Long challengeId,
    Long userId,
    String reason
) {
    public UserLeftEvent {
        if (challengeId == null || challengeId <= 0) {
            throw new IllegalArgumentException("challengeId는 양수여야 합니다");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason은 필수입니다");
        }
    }
}

