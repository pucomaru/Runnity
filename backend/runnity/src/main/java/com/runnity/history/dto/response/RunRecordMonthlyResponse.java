package com.runnity.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "월간 운동 기록 조회 응답 DTO")
public record RunRecordMonthlyResponse(
        @Schema(description = "개인 달리기 기록 목록 데이터")
        List<RunRecordResponse> personals,

        @Schema(description = "챌린지 달리기 기록 목록 데이터")
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
