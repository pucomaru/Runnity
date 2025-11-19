package com.runnity.global.response;

import java.util.List;

public record ValidationErrorResponse(
        List<FieldErrorDetail> errors
) {

    public record FieldErrorDetail(
            String field,
            String message,
            Object rejectedValue
    ) {}
}

