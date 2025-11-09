package com.runnity.stream.socket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BroadcastResponse {
    private Long challengeId;          // 중계 challengeId
    private String title;              // 챌린지 제목
    private Integer viewerCount;       // 현재 시청자 수
    private Integer participantCount;  // 참가자 수
    private String createdAt;          // 세션 생성 시각

}
