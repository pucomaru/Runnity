package com.runnity.history.dto.response;

import com.runnity.history.domain.RunRecord;
import com.runnity.history.domain.RunRecordType;
import lombok.Builder;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Builder
public record RunRecordResponse(
        Long runRecordId,
        Float distance,
        Integer durationSec,
        String startAt,
        String endAt,
        Float pace,
        Integer bpm,
        RunRecordType runType,
        Float calories,
        String route
) {
    public static RunRecordResponse from(RunRecord record) {
        return RunRecordResponse.builder()
                .runRecordId(record.getRunRecordId())
                .distance(record.getDistance())
                .durationSec(record.getDurationSec())
                .startAt(record.getStartAt()
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT))
                .endAt(record.getEndAt()
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT))
                .pace(record.getPace().floatValue())
                .bpm(record.getBpm())
                .runType(record.getRunType())
                .calories(record.getCalories())
                .route(record.getRoute())
                .build();
    }
}
