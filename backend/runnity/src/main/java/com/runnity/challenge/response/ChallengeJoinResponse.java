package com.runnity.challenge.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "챌린지 참가 신청 응답 DTO")
public record ChallengeJoinResponse(
        @Schema(description = "참가자 ID", example = "501")
        Long participantId,

        @Schema(description = "챌린지 ID", example = "1")
        Long challengeId,

        @Schema(description = "회원 ID", example = "101")
        Long memberId,

        @Schema(description = "참가 상태", example = "WAITING")
        String status,

        @Schema(description = "순위", example = "null")
        Integer rank,

        @Schema(description = "평균 페이스", example = "5300")
        Integer averagePace
) {
}

