package com.factoryflow.kpi.api;

import com.factoryflow.kpi.domain.KpiAlias;
import com.factoryflow.kpi.domain.KpiDefinition;
import java.math.BigDecimal;
import java.util.List;

public record KpiDefinitionResponse(
        Long id,
        String code,
        String displayName,
        String category,
        String unit,
        BigDecimal plausibleMin,
        BigDecimal plausibleMax,
        List<String> aliases,
        boolean active
) {
    public static KpiDefinitionResponse from(KpiDefinition definition) {
        return new KpiDefinitionResponse(
                definition.getId(), definition.getCode(), definition.getDisplayName(), definition.getCategory(),
                definition.getUnit(), definition.getPlausibleMin(), definition.getPlausibleMax(),
                definition.getAliases().stream().map(KpiAlias::getAlias).sorted().toList(), definition.isActive()
        );
    }
}
