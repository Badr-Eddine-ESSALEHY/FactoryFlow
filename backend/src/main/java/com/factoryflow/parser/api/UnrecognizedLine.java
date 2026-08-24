package com.factoryflow.parser.api;

import java.util.List;

public record UnrecognizedLine(
        String lineId,
        String sourceLine,
        String reason,
        String sourceLabel,
        List<KpiSuggestion> suggestions
) {
}
