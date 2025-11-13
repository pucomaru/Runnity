package com.runnity.history.dto.response;

import com.runnity.history.domain.RunLap;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "달리기 lap 응답 DTO")
public record RunLapResponse(
        @Schema(description = "lap 번호", example = "1")
        Integer sequence,

        @Schema(description = "거리", example = "5.0")
        Float distance,

        @Schema(description = "lap 진행 시간", example = "60")
        Integer durationSec,

        @Schema(description = "페이스", example = "7")
        Integer pace,

        @Schema(description = "bpm", example = "120")
        Integer bpm
) {
    public static RunLapResponse from(RunLap lap) {
        return RunLapResponse.builder()
                .sequence(lap.getSequence())
                .distance(lap.getDistance())
                .durationSec(lap.getDurationSec())
                .pace(lap.getPace())
                .bpm(lap.getBpm())
                .build();
    }
}
