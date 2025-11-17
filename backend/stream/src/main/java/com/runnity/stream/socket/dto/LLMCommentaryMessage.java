package com.runnity.stream.socket.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMCommentaryMessage {

    private Long challengeId;

    private Long runnerId;
    private String nickname;
    private String profileImage;

    private String highlightType;    // OVERTAKE, TOP3_ENTRY, FINISH, etc.
    private String commentary;       // LLM이 생성한 멘트

    private Long targetRunnerId;
    private String targetNickname;
    private String targetProfileImage;

    private Double distance;
    private Double pace;
    private Integer ranking;
}
