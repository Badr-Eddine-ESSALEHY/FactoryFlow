package com.factoryflow.report.api;

import com.factoryflow.report.domain.UnknownLineResolution;
import jakarta.validation.constraints.NotNull;

public record UnknownLineResolutionRequest(
        @NotNull Long lineId,
        @NotNull UnknownLineResolution resolution,
        Long resolvedKpiDefinitionId
) {
}
