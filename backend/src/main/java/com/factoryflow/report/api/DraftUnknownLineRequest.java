package com.factoryflow.report.api;

import com.factoryflow.report.domain.UnknownLineResolution;
import com.factoryflow.report.domain.UnknownLineKind;
import jakarta.validation.constraints.NotBlank;

public record DraftUnknownLineRequest(
        @NotBlank String sourceLine,
        UnknownLineResolution resolution,
        Long resolvedKpiDefinitionId,
        UnknownLineKind kind,
        String classificationReason,
        boolean safeToIgnore
) {
    public DraftUnknownLineRequest {
        resolution = resolution == null ? UnknownLineResolution.UNRESOLVED : resolution;
        kind = kind == null ? UnknownLineKind.KPI_LIKE : kind;
        classificationReason = classificationReason == null || classificationReason.isBlank()
                ? "UNCLASSIFIED"
                : classificationReason.strip();
        safeToIgnore = safeToIgnore && kind == UnknownLineKind.SAFE_NOISE;
    }

    public DraftUnknownLineRequest(String sourceLine, UnknownLineResolution resolution, Long resolvedKpiDefinitionId) {
        this(sourceLine, resolution, resolvedKpiDefinitionId, UnknownLineKind.KPI_LIKE, "UNCLASSIFIED", false);
    }
}
