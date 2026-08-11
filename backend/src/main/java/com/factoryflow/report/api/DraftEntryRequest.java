package com.factoryflow.report.api;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.Set;

public record DraftEntryRequest(
        Long kpiDefinitionId,
        String sourceLabel,
        String sourceLine,
        BigDecimal extractedValue,
        BigDecimal currentValue,
        @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal confidenceScore,
        boolean editedByUser,
        String capturedUnit,
        Set<String> warnings
) {
    public DraftEntryRequest {
        warnings = warnings == null ? Set.of() : Set.copyOf(warnings);
    }
}
