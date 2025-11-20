package com.runnity.challenge.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.runnity.challenge.domain.Challenge;
import com.runnity.challenge.domain.ChallengeDistance;
import com.runnity.challenge.domain.ChallengeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(description = "챌린지 응답 DTO")
public record ChallengeResponse(
        @Schema(description = "챌린지 ID", example = "1")
        Long challengeId,

        @Schema(description = "챌린지 제목", example = "아침 6시 러닝 챌린지")
        String title,

        @Schema(description = "챌린지 상태", example = "RECRUITING")
        ChallengeStatus status,

        @Schema(description = "현재 참가자 수", example = "15")
        Integer currentParticipants,

        @Schema(description = "최대 참가자 수", example = "20")
        Integer maxParticipants,

        @Schema(description = "시작 일시", example = "2025-11-03T18:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime startAt,

        @Schema(description = "종료 일시", example = "2025-11-03T21:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime endAt,

        @Schema(description = "챌린지 설명", example = "매일 아침 6시에 함께 달리는 러닝 챌린지입니다.")
        String description,

        @Schema(description = "거리", example = "FIVE")
        ChallengeDistance distance,

        @Schema(description = "비밀방 여부", example = "false")
        Boolean isPrivate,

        @Schema(description = "중계방 여부", example = "false")
        Boolean isBroadcast,

        @Schema(description = "로그인 사용자의 참가 여부", example = "true")
        Boolean joined,

        @Schema(description = "참여자 리스트", example = "[{memberId: 1, nickname: '러너A'}, ...]")
        List<ChallengeParticipantResponse> participants,

        @Schema(description = "생성 일시", example = "2025-11-03T10:55:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @Schema(description = "수정 일시", example = "2025-11-03T10:55:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {
    public static ChallengeResponse from(
            Challenge challenge,
            int currentParticipants,
            boolean joined,
            List<ChallengeParticipantResponse> participants
    ) {
        return ChallengeResponse.builder()
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .status(challenge.getStatus())
                .currentParticipants(currentParticipants)
                .maxParticipants(challenge.getMaxParticipants())
                .startAt(challenge.getStartAt())
                .endAt(challenge.getEndAt())
                .description(challenge.getDescription())
                .distance(challenge.getDistance())
                .isPrivate(challenge.getIsPrivate())
                .isBroadcast(challenge.getIsBroadcast())
                .joined(joined)
                .participants(participants)
                .createdAt(challenge.getCreatedAt())
                .updatedAt(challenge.getUpdatedAt())
                .build();
    }
}
