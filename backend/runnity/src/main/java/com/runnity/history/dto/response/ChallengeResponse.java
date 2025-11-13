package com.runnity.history.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.runnity.challenge.domain.Challenge;
import com.runnity.challenge.domain.ChallengeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Builder
@Schema(description = "챌린지 응답 DTO")
public record ChallengeResponse(
        @Schema(description = "챌린지 ID", example = "1")
        Long challengeId,

        @Schema(description = "챌린지 제목", example = "아침 6시 러닝 챌린지")
        String title,

        @Schema(description = "챌린지 상태", example = "RECRUITING")
        ChallengeStatus status,

        @Schema(description = "최대 참가자 수", example = "20")
        Integer maxParticipants,

        @Schema(description = "현재 참가자 수", example = "15")
        Integer currentParticipants,

        @Schema(description = "시작 일시", example = "2025-11-03T18:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        String startAt,  // ISO8601 format

        @Schema(description = "종료 일시", example = "2025-11-03T21:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        String endAt,    // ISO8601 format

        @Schema(description = "거리", example = "5.0")
        Float distance
) {
    public static ChallengeResponse from(Challenge challenge, Integer currentParticipants) {
        return ChallengeResponse.builder()  // @Builder 덕분에 builder() 사용 가능
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .status(challenge.getStatus())
                .maxParticipants(challenge.getMaxParticipants())
                .currentParticipants(currentParticipants)
                .startAt(challenge.getStartAt()
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT))
                .endAt(challenge.getEndAt()
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT))
                .distance(challenge.getDistance().value())
                .build();
    }
}
