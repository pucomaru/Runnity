package com.runnity.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "특정 기간 운동 기록 통계(월, 화, 수, ...) 응답 DTO")
public record PeriodStatResponse(
        @Schema(description = "기간 이름", example = "월")
        String label,

        @Schema(description = "총 거리", example = "5.0")
        Float distance,

        @Schema(description = "총 시간", example = "300")
        Integer time,

        @Schema(description = "평균 페이스", example = "7")
        Integer pace,

        @Schema(description = "러닝 횟수", example = "3")
        Integer count
) {
}
