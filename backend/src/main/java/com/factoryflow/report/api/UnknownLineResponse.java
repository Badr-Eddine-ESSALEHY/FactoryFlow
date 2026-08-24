package com.factoryflow.report.api;

import com.factoryflow.report.domain.ReportUnrecognizedLine;
import com.factoryflow.report.domain.UnknownLineResolution;
import com.factoryflow.report.domain.UnknownLineKind;

public record UnknownLineResponse(
        Long id,
        String sourceLine,
        UnknownLineResolution resolution,
        Long resolvedKpiDefinitionId,
        UnknownLineKind kind,
        String classificationReason,
        boolean safeToIgnore
) {
    static UnknownLineResponse from(ReportUnrecognizedLine line) {
        return new UnknownLineResponse(
                line.getId(), line.getSourceLine(), line.getResolution(),
                line.getResolvedDefinition() == null ? null : line.getResolvedDefinition().getId(),
                line.getKind(), line.getClassificationReason(), line.isSafeToIgnore()
        );
    }
}
