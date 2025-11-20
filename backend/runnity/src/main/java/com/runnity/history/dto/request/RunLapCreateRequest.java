package com.runnity.history.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "러닝 랩 생성 요청 DTO")
public record RunLapCreateRequest(

        @NotNull(message = "랩 순서는 필수입니다.")
        @Schema(description = "랩 순서 (1부터 시작)", example = "1")
        Integer sequence,

        @NotNull(message = "랩 거리(km)는 필수입니다.")
        @Schema(description = "랩 거리 (km)", example = "0.87")
        Float distance,

        @NotNull(message = "랩 구간 소요 시간은 필수입니다.")
        @Schema(description = "랩 소요 시간 (초)", example = "60")
        Integer durationSec,

        @NotNull(message = "랩 페이스는 필수입니다.")
        @Schema(description = "랩 페이스 (초/km)", example = "410")
        Integer pace,

        @NotNull(message = "랩 심박수는 필수입니다.")
        @Schema(description = "랩 평균 심박수 (bpm)", example = "145")
        Integer bpm
) {}
