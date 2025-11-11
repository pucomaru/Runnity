package com.runnity.stream.socket.dto;

import lombok.Builder;
import lombok.Data;

// llm 서버로 보내는 하이라이트 이벤트 메시지
// highlight-event 토픽에 publish

@Data
@Builder
public class HighlightEventDto {

    private String highlightType;

    private Long challengeId;

    private Long runnerId;
    private String nickname;
    private String profileImage;

    // (overtake 등 타겟이 있는 경우)
    private Long targetRunnerId;
    private String targetNickname;
    private String targetProfileImage;

    private Double distance;
    private Double pace;
    private Integer ranking;

}
