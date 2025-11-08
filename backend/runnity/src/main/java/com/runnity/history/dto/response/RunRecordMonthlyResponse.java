package com.runnity.history.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record RunRecordMonthlyResponse(
        List<RunRecordResponse> personals,
        List<RunRecordResponse> challenges
) {
    public static RunRecordMonthlyResponse of(
            List<RunRecordResponse> personals,
            List<RunRecordResponse> challenges
    ) {
        return RunRecordMonthlyResponse.builder()
                .personals(personals)
                .challenges(challenges)
                .build();
    }
}
