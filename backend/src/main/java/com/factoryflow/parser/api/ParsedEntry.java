package com.factoryflow.parser.api;

import java.math.BigDecimal;
import java.util.List;

public record ParsedEntry(
        String candidateId,
        Long kpiDefinitionId,
        String kpiCode,
        String kpiDisplayName,
        String sourceLabel,
        String sourceLine,
        BigDecimal extractedValue,
        String capturedUnit,
        String expectedUnit,
        BigDecimal confidenceScore,
        String confidenceLevel,
        String matchMethod,
        String reviewState,
        List<ParserWarning> warnings,
        List<KpiSuggestion> suggestions,
        BigDecimal secondaryExtractedValue,
        String secondaryUnit
) {
}
