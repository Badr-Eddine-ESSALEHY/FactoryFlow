package com.factoryflow.parser.application;

import com.factoryflow.shared.text.TextNormalizer;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SourceLineClassifier {

    private static final Set<String> HEADERS = Set.of(
            "kpi",
            "kpis",
            "indicateur",
            "indicateurs",
            "indicateurs du jour",
            "indicateurs quotidiens",
            "kpi du jour",
            "kpi quotidiens",
            "maintenance",
            "rapport kpi",
            "rapport maintenance",
            "rapport de maintenance",
            "kpi production"
    );

    private static final Set<String> WHATSAPP_SEPARATORS = Set.of(
            "aujourd hui",
            "hier",
            "message"
    );

    private static final Set<String> KNOWN_CONTEXTUAL_OCR_NOISE = Set.of(
            "pf",
            "aymane",
            "lokbiche"
    );

    private static final Pattern ISOLATED_OCR_NOISE = Pattern.compile(
            "^(?:[\\p{L}]|[\\p{Punct}…•·]+)$",
            Pattern.UNICODE_CHARACTER_CLASS
    );

    private static final Pattern SOURCE_DATE_HEADER = Pattern.compile(
            "^(?i:de)\\s+\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}$"
    );

    private static final Pattern KNOWN_CONVERSATION_CONTEXT = Pattern.compile(
            "^(?i:suivi\\s+des\\s+consommations(?:\\s+liquides)?)[.!…]*$"
    );

    private static final Pattern SITE_SENDER_METADATA = Pattern.compile(
            "^(?=.*(?i:\\balf\\b))(?!.*\\d)(?!.*(?:->|→|=>|:|=)).+$"
    );

    private static final Pattern TIME_ONLY = Pattern.compile(
            "^\\[?\\d{1,2}:\\d{2}(?::\\d{2})?\\]?$"
    );

    private static final Pattern TIME_PREFIXED_METADATA = Pattern.compile(
            "^\\[?\\d{1,2}:\\d{2}(?::\\d{2})?\\]?(?:\\s*[-–—:]?\\s+.+)$"
    );

    private static final Pattern DATE_ONLY = Pattern.compile(
            "^(?:\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|\\d{4}[/-]\\d{1,2}[/-]\\d{1,2})$"
    );

    private static final Pattern DATE_PREFIXED_METADATA = Pattern.compile(
            "^(?:\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|\\d{4}[/-]\\d{1,2}[/-]\\d{1,2})"
                    + "(?:\\s+.+)$"
    );

    private static final Pattern BRACKETED_TIME_PREFIX = Pattern.compile(
            "^\\[\\d{1,2}:\\d{2}(?::\\d{2})?\\]\\s*.+$"
    );

    public Classification classify(String sourceLine) {
        if (sourceLine == null || sourceLine.isBlank()) {
            return new Classification(
                    LineType.EMPTY,
                    "EMPTY"
            );
        }

        String structural =
                TextNormalizer.normalizeStructuralText(sourceLine);

        String normalized =
                TextNormalizer.normalizeLabel(structural);

        if (isHeader(normalized)) {
            return new Classification(
                    LineType.HEADER,
                    "HEADER"
            );
        }

        if (isWhatsAppMetadata(structural)) {
            return new Classification(
                    LineType.WHATSAPP_METADATA,
                    "WHATSAPP_METADATA"
            );
        }

        return new Classification(
                LineType.CONTENT,
                "CONTENT"
        );
    }

    private boolean isHeader(String normalized) {
        if (HEADERS.contains(normalized)) {
            return true;
        }

        return normalized.matches(
                "^(?:kpi|kpis|indicateurs?)"
                        + "(?:\\s+(?:du jour|quotidiens?|maintenance|production))?$"
        );
    }

    private boolean isWhatsAppMetadata(String source) {
        String value = source.strip();
        String normalized = TextNormalizer.normalizeLabel(value);

        return TIME_ONLY.matcher(value).matches()
                || BRACKETED_TIME_PREFIX.matcher(value).matches()
                || TIME_PREFIXED_METADATA.matcher(value).matches()
                || DATE_ONLY.matcher(value).matches()
                || DATE_PREFIXED_METADATA.matcher(value).matches()
                || SOURCE_DATE_HEADER.matcher(value).matches()
                || WHATSAPP_SEPARATORS.contains(normalized)
                || KNOWN_CONTEXTUAL_OCR_NOISE.contains(normalized)
                || ISOLATED_OCR_NOISE.matcher(value).matches()
                || KNOWN_CONVERSATION_CONTEXT.matcher(value).matches()
                || SITE_SENDER_METADATA.matcher(value).matches();
    }

    public enum LineType {
        EMPTY,
        HEADER,
        WHATSAPP_METADATA,
        CONTENT
    }

    public record Classification(
            LineType type,
            String reason
    ) {
        public boolean ignored() {
            return type == LineType.EMPTY
                    || type == LineType.HEADER
                    || type == LineType.WHATSAPP_METADATA;
        }
    }
}
