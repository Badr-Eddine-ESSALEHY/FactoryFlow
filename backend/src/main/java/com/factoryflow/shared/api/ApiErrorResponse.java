package com.factoryflow.shared.api;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        List<ApiErrorDetail> details
) {
    public ApiErrorResponse {
        details = details == null ? List.of() : List.copyOf(details);
    }
}
