package com.runnity.stream.socket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


// STOMP로 송출되는 데이터
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BroadcastFrameDto {

    private Long challengeId;
    private Long memberId;      
    private Double distance;    // 누적 거리
    private Double pace;        // 페이스
    private Integer ranking;    // 실시간 순위(Redis SortedSet기반)
    private Long timestamp;     // 해당 시간
    private String eventType;   //update,finish등 이벤트


}
