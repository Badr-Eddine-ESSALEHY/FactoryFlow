package com.factoryflow.report.api;

import com.factoryflow.report.domain.UnknownLineResolution;
import jakarta.validation.constraints.NotBlank;

public record DraftUnknownLineRequest(
        @NotBlank String sourceLine,
        UnknownLineResolution resolution,
        Long resolvedKpiDefinitionId
) {
    public DraftUnknownLineRequest {
        resolution = resolution == null ? UnknownLineResolution.UNRESOLVED : resolution;
    }
}
