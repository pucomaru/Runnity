package com.runnity.history.dto.response;

import lombok.Builder;

@Builder
public record PeriodStatResponse(
        String label,
        Float distance,
        Integer time,
        Integer pace,
        Integer count
) {
}
