package com.runnity.websocket.dto.websocket.server;

import java.util.List;

/**
 * CONNECTED 메시지 (서버 → 클라이언트)
 * 
 * WebSocket 연결 성공 시 전송되는 메시지입니다.
 * 본인 정보와 현재 참여 중인 다른 참가자 목록을 함께 전송합니다.
 * 
 * @param type 메시지 타입 ("CONNECTED")
 * @param challengeId 챌린지 ID (디버깅 및 검증용)
 * @param userId 사용자 ID (디버깅 및 클라이언트 상태 확인용)
 * @param me 본인 정보
 * @param participants 현재 참여 중인 다른 참가자 목록 (본인 제외)
 * @param timestamp 타임스탬프
 */
public record ConnectedMessage(
    String type,
    Long challengeId,
    Long userId,
    Participant me,
    List<Participant> participants,
    Long timestamp
) {
    public ConnectedMessage {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type은 필수입니다");
        }
        if (challengeId == null) {
            throw new IllegalArgumentException("challengeId는 필수입니다");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다");
        }
        if (me == null) {
            throw new IllegalArgumentException("me는 필수입니다");
        }
        if (participants == null) {
            throw new IllegalArgumentException("participants는 필수입니다");
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }
    
    public ConnectedMessage(Long challengeId, Long userId, Participant me, List<Participant> participants) {
        this("CONNECTED", challengeId, userId, me, participants, System.currentTimeMillis());
    }
    
    /**
     * 참가자 정보
     * 
     * @param userId 사용자 ID
     * @param nickname 닉네임
     * @param profileImage 프로필 이미지 URL
     * @param distance 현재 거리 (km)
     * @param pace 현재 페이스 (분/km)
     */
    public record Participant(
        Long userId,
        String nickname,
        String profileImage,
        Double distance,
        Double pace
    ) {
        public Participant {
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
        }
    }
}

