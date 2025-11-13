package com.runnity.websocket.dto.websocket.server;

/**
 * PARTICIPANT_UPDATE 메시지 (서버 → 클라이언트)
 * 
 * 다른 참가자의 distance, pace가 업데이트될 때 전송되는 메시지입니다.
 * 프론트: 해당 참가자 정보만 업데이트
 * 
 * @param type 메시지 타입 ("PARTICIPANT_UPDATE")
 * @param userId 업데이트된 사용자 ID
 * @param distance 현재 거리 (km)
 * @param pace 현재 페이스 (분/km)
 * @param timestamp 타임스탬프
 */
public record ParticipantUpdateMessage(
    String type,
    Long userId,
    Double distance,
    Double pace,
    Long timestamp
) {
    public ParticipantUpdateMessage {
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
    
    public ParticipantUpdateMessage(Long userId, Double distance, Double pace) {
        this("PARTICIPANT_UPDATE", userId, distance, pace, System.currentTimeMillis());
    }
}
