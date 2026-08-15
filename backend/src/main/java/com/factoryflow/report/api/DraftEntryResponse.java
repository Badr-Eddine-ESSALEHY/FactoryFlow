package com.factoryflow.report.api;

import com.factoryflow.report.domain.KpiEntry;
import java.math.BigDecimal;
import java.util.Set;

public record DraftEntryResponse(
        Long id,
        Long kpiDefinitionId,
        String kpiCode,
        String kpiDisplayName,
        String sourceLabel,
        String sourceLine,
        BigDecimal extractedValue,
        BigDecimal currentValue,
        BigDecimal finalValue,
        BigDecimal confidenceScore,
        boolean editedByUser,
        String capturedUnit,
        Set<String> warnings,
        Long suggestedKpiDefinitionId,
        String suggestedKpiDisplayName,
        String suggestedKpiUnit,
        BigDecimal suggestionScore,
        BigDecimal secondaryExtractedValue,
        BigDecimal secondaryCurrentValue,
        BigDecimal secondaryFinalValue,
        String secondaryUnit
) {
    static DraftEntryResponse from(KpiEntry entry) {
        return new DraftEntryResponse(
                entry.getId(),
                entry.getDefinition() == null ? null : entry.getDefinition().getId(),
                entry.getDefinition() == null ? null : entry.getDefinition().getCode(),
                entry.getDefinition() == null ? null : entry.getDefinition().getDisplayName(),
                entry.getSourceLabel(), entry.getSourceLine(), entry.getExtractedValue(), entry.getCurrentValue(),
                entry.getFinalValue(), entry.getConfidenceScore(), entry.isEditedByUser(), entry.getCapturedUnit(),
                entry.getWarningCodes(),
                entry.getSuggestedDefinition() == null ? null : entry.getSuggestedDefinition().getId(),
                entry.getSuggestedDefinition() == null ? null : entry.getSuggestedDefinition().getDisplayName(),
                entry.getSuggestedDefinition() == null ? null : entry.getSuggestedDefinition().getUnit(),
                entry.getSuggestionScore(), entry.getSecondaryExtractedValue(), entry.getSecondaryCurrentValue(),
                entry.getSecondaryFinalValue(), entry.getSecondaryUnit()
        );
    }
}
