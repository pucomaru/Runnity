package com.runnity.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "내가 참가 신청한 챌린지 응답 DTO")
public record MyChallengesResponse (
        @Schema(description = "현재 참여 가능한 챌린지 데이터")
        ChallengeResponse enterableChallenge,

        @Schema(description = "참가 신청한 챌린지 목록 데이터")
        List<ChallengeResponse> joinedChallenges
){
}
