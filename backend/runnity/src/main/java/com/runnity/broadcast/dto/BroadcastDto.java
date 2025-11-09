package com.runnity.broadcast.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastDto {
    private Long challengeId;   // 중계 challengeId
    private String title;       // 챌린지 제목
    private Integer viewerCount; // 현재 시청자 수
    private Integer participantCount;   // 챌린지 참가자 수
    private String createdAt;   // 세션 생성 시작

}
