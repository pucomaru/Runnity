package com.runnity.history.dto.request;

import com.runnity.history.domain.RunRecordType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "개인 러닝 기록 생성 요청 DTO")
public record RunRecordCreateRequest(


        @NotNull(message = "러닝 타입은 필수입니다.")
        @Schema(description = "러닝 타입 (ENUM: PERSONAL, CHALLENGE)", example = "PERSONAL")
        RunRecordType runType,

        @NotNull(message = "총 거리는 필수입니다.")
        @Schema(description = "총 거리 (km)", example = "5.2")
        Float distance,

        @NotNull(message = "총 소요 시간은 필수입니다.")
        @Schema(description = "총 소요 시간 (초)", example = "1200")
        Integer durationSec,

        @NotNull(message = "시작 시각은 필수입니다.")
        @Schema(description = "시작 시각 (ISO-8601 형식)", example = "2025-11-07T09:30:00Z")
        LocalDateTime startAt,

        @NotNull(message = "종료 시각은 필수입니다.")
        @Schema(description = "종료 시각 (ISO-8601 형식)", example = "2025-11-07T09:50:00Z")
        LocalDateTime endAt,

        @NotNull(message = "평균 페이스는 필수입니다.")
        @Schema(description = "평균 페이스 (초/km)", example = "230")
        Integer pace,

        @NotNull(message = "평균 심박수는 필수입니다.")
        @Schema(description = "평균 심박수 (bpm)", example = "148")
        Integer bpm,

        @NotNull(message = "소모 칼로리는 필수입니다.")
        @Schema(description = "소모 칼로리 (kcal)", example = "250.0")
        Float calories,

        @NotNull(message = "러닝 경로(route)는 필수입니다.")
        @Schema(description = "러닝 경로 (JSON 문자열 형태)", example = "{\"coordinates\": [...]} ")
        String route,

        @NotNull(message = "랩(lap) 정보는 필수입니다.")
        @Schema(description = "랩 구간 정보 리스트")
        List<RunLapCreateRequest> laps
) {}
