package com.factoryflow.shared.api;

public record ApiErrorDetail(
        String field,
        String message
) {
}
