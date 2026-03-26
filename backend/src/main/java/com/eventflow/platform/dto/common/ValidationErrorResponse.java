package com.eventflow.platform.dto.common;

import java.time.OffsetDateTime;
import java.util.List;

public record ValidationErrorResponse(
        boolean success,
        String code,
        String message,
        List<FieldErrorDto> errors,
        OffsetDateTime timestamp) {
}
