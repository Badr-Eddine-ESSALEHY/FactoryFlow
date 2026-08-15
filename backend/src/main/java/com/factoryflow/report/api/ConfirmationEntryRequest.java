package com.factoryflow.report.api;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ConfirmationEntryRequest(
        @NotNull Long kpiDefinitionId,
        BigDecimal finalValue,
        BigDecimal secondaryFinalValue
) {
    public ConfirmationEntryRequest(Long kpiDefinitionId, BigDecimal finalValue) {
        this(kpiDefinitionId, finalValue, null);
    }
}
