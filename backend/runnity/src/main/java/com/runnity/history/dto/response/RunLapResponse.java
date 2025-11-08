package com.runnity.history.dto.response;

import com.runnity.history.domain.RunLap;
import lombok.Builder;

@Builder
public record RunLapResponse(
        Integer sequence,
        Float distance,
        Integer durationSec,
        Integer pace,
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
