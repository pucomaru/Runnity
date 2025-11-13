package com.runnity.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "기간별 운동 기록 조회 응답 DTO")
public record StatsSummaryResponse(
        @Schema(description = "총 거리", example = "5.0")
        Float totalDistance,

        @Schema(description = "총 거리", example = "150")
        Integer totalTime,

        @Schema(description = "평균 페이스", example = "7")
        Integer avgPace,

        @Schema(description = "달린 날 횟수", example = "7")
        Integer totalRunDays,

        @Schema(description = "세부 구간별 목록 데이터")
        List<PeriodStatResponse> periodStats,

        @Schema(description = "최신 개인 달리기 5개 목록 데이터")
        List<RunRecordResponse> personals,

        @Schema(description = "최신 챌린지 5개 목록 데이터")
        List<RunRecordResponse> challenges   
) {
}
