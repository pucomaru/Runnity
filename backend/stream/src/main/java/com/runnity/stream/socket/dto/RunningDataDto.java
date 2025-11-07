package com.runnity.stream.socket.dto;

import lombok.Data;

// kafka "running-data" 토픽 메시지 파싱용 DTO
// isBroadcast =true -> 방송 세션 자동 생성

@Data
public class RunningDataDto {
    private Long challengeId;
    private String title;
    private int participantCount;
    private Long userId;
    private boolean isBroadcast;
    private double distance;
    private double pace;
    private String timestamp;

}
