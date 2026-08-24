package com.factoryflow.report.api;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ConfirmationEntryRequest(
        @NotNull Long entryId,
        @NotNull Long kpiDefinitionId,
        BigDecimal finalValue,
        BigDecimal secondaryFinalValue
) {
    public ConfirmationEntryRequest(Long entryId, Long kpiDefinitionId, BigDecimal finalValue) {
        this(entryId, kpiDefinitionId, finalValue, null);
    }

    /**
     * Kept for source compatibility in unit tests that do not yet persist draft entry IDs.
     * API clients must submit {@code entryId}; Bean Validation rejects this legacy shape.
     */
    public ConfirmationEntryRequest(Long kpiDefinitionId, BigDecimal finalValue) {
        this(null, kpiDefinitionId, finalValue, null);
    }
}
