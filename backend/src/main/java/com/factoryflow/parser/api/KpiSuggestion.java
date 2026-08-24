package com.factoryflow.parser.api;

import java.math.BigDecimal;

public record KpiSuggestion(
        Long kpiDefinitionId,
        String kpiCode,
        String displayName,
        String unit,
        BigDecimal score,
        String matchMethod,
        String strength
) {
    public KpiSuggestion(Long kpiDefinitionId, String kpiCode, String displayName, String unit,
                         BigDecimal score, String matchMethod) {
        this(kpiDefinitionId, kpiCode, displayName, unit, score, matchMethod, "WEAK");
    }
}
