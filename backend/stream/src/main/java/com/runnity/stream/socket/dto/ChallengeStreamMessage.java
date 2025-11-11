package com.runnity.stream.socket.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeStreamMessage {

    private String eventType;      // start, running, finish, leave
    private Long challengeId;      // 챌린지 ID
    private Long runnerId;         // 러너 ID

    // 추가
    private String nickname;
    private String profileImage;

    private Double distance;       // 누적거리
    private Double pace;           // 페이스
    private Integer ranking;       // 순위
    private Boolean isBroadcast;   // 중계방 여부
    private String reason;         // leave 시 사유 (QUIT, TIMEOUT, DISCONNECTED, KICKED, ERROR, EXPIRED)
}
