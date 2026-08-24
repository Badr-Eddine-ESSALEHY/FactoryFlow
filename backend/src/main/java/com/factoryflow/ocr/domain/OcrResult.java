package com.factoryflow.ocr.domain;

import java.math.BigDecimal;
import java.util.List;

public record OcrResult(
        String fullText,
        List<Line> lines,
        BigDecimal confidence,
        String engine,
        long processingTimeMs,
        List<String> warnings
) {
    public OcrResult {
        fullText = fullText == null ? "" : fullText.strip();
        lines = lines == null ? List.of() : List.copyOf(lines);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public record Line(String text, BigDecimal confidence, BoundingBox boundingBox) { }
    public record BoundingBox(int left, int top, int right, int bottom) { }
}
