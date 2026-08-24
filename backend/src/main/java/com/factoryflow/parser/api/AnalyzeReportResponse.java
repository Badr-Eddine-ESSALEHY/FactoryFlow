package com.factoryflow.parser.api;

import com.factoryflow.report.domain.AcquisitionSource;
import java.util.List;

public record AnalyzeReportResponse(
        AcquisitionSource source,
        String rawText,
        int recognizedCount,
        int readyCount,
        int attentionCount,
        int missingCount,
        int unresolvedCount,
        int needsReviewCount,
        int unrecognizedCount,
        List<ParsedEntry> entries,
        List<UnrecognizedLine> unrecognizedLines,
        List<IgnoredSourceLine> ignoredLines
) {
}
