package com.runnity.challenge.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.runnity.challenge.domain.Challenge;
import com.runnity.challenge.domain.ChallengeDistance;
import com.runnity.challenge.domain.ChallengeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "챌린지 목록 아이템 응답")
public record ChallengeListItemResponse(

        @Schema(description = "챌린지 ID", example = "1")
        Long challengeId,

        @Schema(description = "제목", example = "아침 6시 러닝 챌린지")
        String title,

        @Schema(description = "상태", example = "RECRUITING")
        ChallengeStatus status,

        @Schema(description = "최대 참가자 수", example = "20")
        Integer maxParticipants,

        @Schema(description = "현재 참가자 수", example = "15")
        Integer currentParticipants,

        @Schema(description = "시작 일시", example = "2025-11-06T06:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime startAt,

        @Schema(description = "종료 일시", example = "2025-11-06T07:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime endAt,

        @Schema(description = "거리", example = "FIVE")
        ChallengeDistance distance,

        @Schema(description = "비밀방 여부", example = "false")
        Boolean isPrivate,

        @Schema(description = "중계방 여부", example = "false")
        Boolean isBroadcast,

        @Schema(description = "참가 여부", example = "true")
        Boolean isJoined,

        @Schema(description = "생성 일시", example = "2025-11-05T10:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {

    public static ChallengeListItemResponse from(
            Challenge challenge,
            int currentParticipants,
            boolean isJoined
    ) {
        return ChallengeListItemResponse.builder()
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .status(challenge.getStatus())
                .maxParticipants(challenge.getMaxParticipants())
                .currentParticipants(currentParticipants)
                .startAt(challenge.getStartAt())
                .endAt(challenge.getEndAt())
                .distance(challenge.getDistance())
                .isPrivate(challenge.getIsPrivate())
                .isBroadcast(challenge.getIsBroadcast())
                .isJoined(isJoined)
                .createdAt(challenge.getCreatedAt())
                .build();
    }
}
