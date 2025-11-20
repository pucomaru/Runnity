package com.runnity.challenge.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "챌린지 참가 신청 요청 DTO")
public record ChallengeJoinRequest(
        @Schema(description = "비밀번호 (비밀방일 경우 필수)", example = "1234")
        String password
) {
}

