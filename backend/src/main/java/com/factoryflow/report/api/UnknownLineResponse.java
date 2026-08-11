package com.factoryflow.report.api;

import com.factoryflow.report.domain.ReportUnrecognizedLine;
import com.factoryflow.report.domain.UnknownLineResolution;

public record UnknownLineResponse(
        Long id,
        String sourceLine,
        UnknownLineResolution resolution,
        Long resolvedKpiDefinitionId
) {
    static UnknownLineResponse from(ReportUnrecognizedLine line) {
        return new UnknownLineResponse(
                line.getId(), line.getSourceLine(), line.getResolution(),
                line.getResolvedDefinition() == null ? null : line.getResolvedDefinition().getId()
        );
    }
}
