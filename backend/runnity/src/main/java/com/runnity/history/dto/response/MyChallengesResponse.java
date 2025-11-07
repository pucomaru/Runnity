package com.runnity.history.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record MyChallengesResponse (
        ChallengeResponse enterableChallenge,
        List<ChallengeResponse> joinedChallenges
){
}
