package com.runnity.websocket.dto.websocket.server;

/**
 * USER_ENTERED 메시지 (서버 → 클라이언트)
 * 
 * 다른 참가자가 챌린지에 입장했을 때 전송되는 메시지입니다.
 * 프론트: 참가자 목록에 추가
 * 
 * @param type 메시지 타입 ("USER_ENTERED")
 * @param userId 입장한 사용자 ID
 * @param nickname 닉네임
 * @param profileImage 프로필 이미지 URL
 * @param distance 초기 거리 (0.0)
 * @param pace 초기 페이스 (0.0)
 * @param timestamp 타임스탬프
 */
public record UserEnteredMessage(
    String type,
    Long userId,
    String nickname,
    String profileImage,
    Double distance,
    Integer pace,
    Long timestamp
) {
    public UserEnteredMessage {
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다");
        }
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("nickname은 필수입니다");
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
    
    public UserEnteredMessage(Long userId, String nickname, String profileImage, Double distance, Integer pace) {
        this("USER_ENTERED", userId, nickname, profileImage, distance, pace, System.currentTimeMillis());
    }
}

