package com.runnity.stream.socket.dto;

import lombok.*;

// Kafka에서 수신되는 데이터

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeRecordMessage {

    private Long challengeId;
    private Long memberId;
    private Double distance;
    private Double pace;
    private Integer ranking;
    private Long timestamp;
    private String eventType;
}
