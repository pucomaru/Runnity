package com.runnity.history.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.runnity.history.domain.RunRecord;
import com.runnity.history.domain.RunRecordType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Builder
@Schema(description = "운동 기록 상세 조회 응답 DTO")
public record RunRecordDetailResponse(
        @Schema(description = "런 레코드 ID", example = "1")
        Long runRecordId,

        @Schema(description = "챌린지 ID (챌린지 달리기인 경우)", example = "1")
        Long challengeId,

        @Schema(description = "거리", example = "5.0")
        Float distance,

        @Schema(description = "lap 진행 시간", example = "60")
        Integer durationSec,

        @Schema(description = "시작 일시", example = "2025-11-03T18:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        String startAt,

        @Schema(description = "종료 일시", example = "2025-11-03T21:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        String endAt,

        @Schema(description = "페이스", example = "7")
        Integer pace,

        @Schema(description = "bpm", example = "120")
        Integer bpm,

        @Schema(description = "달리기 타입(personal/challenge)", example = "personal")
        RunRecordType runType,

        @Schema(description = "칼로리", example = "5.0")
        Float calories,

        @Schema(description = "달린 루트", example = "{\\\"coordinates\\\": [[37.55, 126.97], [37.56, 126.98], [37.57, 126.99]]}")
        String route,

        @Schema(description = "lap 목록 데이터")
        List<RunLapResponse> laps
) {
    public static RunRecordDetailResponse from(RunRecord record, List<RunLapResponse> laps, Long challengeId) {
        return RunRecordDetailResponse.builder()
                .runRecordId(record.getRunRecordId())
                .challengeId(challengeId)
                .distance(record.getDistance())
                .durationSec(record.getDurationSec())
                .startAt(record.getStartAt()
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT))
                .endAt(record.getEndAt()
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT))
                .pace(record.getPace())
                .bpm(record.getBpm())
                .runType(record.getRunType())
                .calories(record.getCalories())
                .route(record.getRoute())
                .laps(laps)
                .build();
    }
}
