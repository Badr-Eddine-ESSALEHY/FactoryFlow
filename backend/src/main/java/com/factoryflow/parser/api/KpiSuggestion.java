package com.factoryflow.parser.api;

import java.math.BigDecimal;

public record KpiSuggestion(
        Long kpiDefinitionId,
        String kpiCode,
        String displayName,
        String unit,
        BigDecimal score,
        String matchMethod
) {
}
