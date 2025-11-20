package com.runnity.websocket.dto.websocket.server;

/**
 * USER_LEFT 메시지 (서버 → 클라이언트)
 * 
 * 다른 참가자가 챌린지에서 퇴장했을 때 전송되는 메시지입니다.
 * 프론트: 참가자 목록에서 제거
 * 
 * @param type 메시지 타입 ("USER_LEFT")
 * @param userId 퇴장한 사용자 ID
 * @param reason 퇴장 사유 (QUIT, FINISH, TIMEOUT, DISCONNECTED, KICKED, EXPIRED, ERROR)
 * @param timestamp 타임스탬프
 */
public record UserLeftMessage(
    String type,
    Long userId,
    String reason,
    Long timestamp
) {
    public UserLeftMessage {
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason은 필수입니다");
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }
    
    public UserLeftMessage(Long userId, String reason) {
        this("USER_LEFT", userId, reason, System.currentTimeMillis());
    }
}
