package com.factoryflow.kpi.api;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

public record KpiDefinitionRequest(
        @NotBlank String code,
        @NotBlank String displayName,
        String category,
        String unit,
        BigDecimal plausibleMin,
        BigDecimal plausibleMax,
        List<String> aliases,
        Boolean active
) {
    public KpiDefinitionRequest {
        aliases = aliases == null ? List.of() : List.copyOf(aliases);
        active = active == null ? Boolean.TRUE : active;
    }
}
