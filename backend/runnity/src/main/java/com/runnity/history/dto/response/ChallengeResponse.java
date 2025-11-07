package com.runnity.history.dto.response;

import com.runnity.challenge.domain.Challenge;
import lombok.Builder;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Builder
public record ChallengeResponse(
        Long challengeId,
        String title,
        String status,
        Integer maxParticipants,
        Integer currentParticipants,
        String startAt,  // ISO8601 format
        String endAt,    // ISO8601 format
        Float distance
) {
    public static ChallengeResponse from(Challenge challenge, Integer currentParticipants) {
        return ChallengeResponse.builder()  // @Builder 덕분에 builder() 사용 가능
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .status(challenge.getStatus().code())
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
