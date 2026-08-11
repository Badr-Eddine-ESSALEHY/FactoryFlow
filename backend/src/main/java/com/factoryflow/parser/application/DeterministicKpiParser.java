package com.factoryflow.parser.application;

import com.factoryflow.kpi.domain.KpiAlias;
import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.parser.api.AnalyzeReportResponse;
import com.factoryflow.parser.api.ParsedEntry;
import com.factoryflow.parser.api.ParserWarning;
import com.factoryflow.parser.api.UnrecognizedLine;
import com.factoryflow.report.domain.AcquisitionSource;
import com.factoryflow.shared.text.TextNormalizer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class DeterministicKpiParser {

    private static final Pattern EXPLICIT_SEPARATOR = Pattern.compile("\\s*(?:->|→|:|=)\\s*");
    private static final Pattern VALUE_START = Pattern.compile("^(.*?)(?:\\s+)(-{3,}|[+-]?\\d.*)$");
    private static final Pattern NUMBER = Pattern.compile("[-+]?\\d(?:[\\d ]*\\d)?(?:[.,]\\d+)?");
    private static final Pattern MISSING = Pattern.compile("^-{3,}$");

    private final ParserProperties properties;

    public DeterministicKpiParser(ParserProperties properties) {
        this.properties = properties;
    }

    public AnalyzeReportResponse parse(String rawText, AcquisitionSource source, List<KpiDefinition> definitions) {
        List<MutableCandidate> candidates = new ArrayList<>();
        List<UnrecognizedLine> unknown = new ArrayList<>();
        int unknownSequence = 1;

        String normalizedLineEndings = rawText.replace("\r\n", "\n").replace('\r', '\n');
        for (String originalLine : normalizedLineEndings.split("\n")) {
            String line = originalLine.strip();
            if (line.isEmpty()) {
                continue;
            }
            LineParts parts = splitLine(line, definitions);
            if (parts == null) {
                unknown.add(new UnrecognizedLine("u" + unknownSequence++, line, "NO_KPI_MATCH"));
                continue;
            }

            Optional<LabelMatch> labelMatch = matchLabel(parts.label(), definitions);
            if (labelMatch.isEmpty()) {
                unknown.add(new UnrecognizedLine("u" + unknownSequence++, line, "NO_KPI_MATCH"));
                continue;
            }

            LabelMatch match = labelMatch.get();
            String valueText = parts.valueText().trim();
            if (MISSING.matcher(valueText).matches()) {
                candidates.add(MutableCandidate.missing(match, parts.label(), line));
                continue;
            }

            String multiValueSafe = valueText.replaceAll("(?<=\\d)-(?=\\d)", "|");
            Matcher numberMatcher = NUMBER.matcher(multiValueSafe);
            int valueIndex = 0;
            while (numberMatcher.find()) {
                NumericValue numeric = normalizeNumber(numberMatcher.group());
                String capturedUnit = capturedUnit(multiValueSafe, numberMatcher.end());
                KpiDefinition definition = valueIndex == 0 ? match.definition() : null;
                List<ParserWarning> warnings = new ArrayList<>(numeric.warnings());
                double confidence = valueIndex == 0 ? match.score() : 0.50;
                if (valueIndex > 0) {
                    warnings.add(ParserWarning.warning(
                            "ADDITIONAL_VALUE_REQUIRES_ASSIGNMENT",
                            "An additional value was found on the same line and requires KPI assignment during review."
                    ));
                } else {
                    validatePlausibility(definition, numeric.value(), warnings);
                    validateUnit(definition, capturedUnit, warnings);
                    if (match.kind().equals("FUZZY")) {
                        warnings.add(ParserWarning.warning("LOW_CONFIDENCE", "The KPI label was matched approximately."));
                    }
                }
                candidates.add(new MutableCandidate(
                        definition, parts.label(), line, numeric.value(), capturedUnit,
                        BigDecimal.valueOf(confidence).setScale(4, RoundingMode.HALF_UP), warnings
                ));
                valueIndex++;
            }
            if (valueIndex == 0) {
                candidates.add(MutableCandidate.invalid(match, parts.label(), line));
            }
        }

        markDuplicates(candidates);
        List<ParsedEntry> entries = new ArrayList<>();
        int candidateSequence = 1;
        for (MutableCandidate candidate : candidates) {
            entries.add(candidate.toResponse("c" + candidateSequence++));
        }
        int recognized = (int) entries.stream().filter(entry -> entry.kpiDefinitionId() != null).count();
        int needsReview = (int) entries.stream().filter(entry -> entry.kpiDefinitionId() == null || !entry.warnings().isEmpty()).count();
        return new AnalyzeReportResponse(source, rawText, recognized, needsReview, unknown.size(), entries, unknown);
    }

    private LineParts splitLine(String line, List<KpiDefinition> definitions) {
        Matcher explicit = EXPLICIT_SEPARATOR.matcher(line);
        if (explicit.find()) {
            return new LineParts(line.substring(0, explicit.start()).trim(), line.substring(explicit.end()).trim());
        }
        Matcher whitespace = VALUE_START.matcher(line);
        if (whitespace.matches()) {
            return new LineParts(whitespace.group(1).trim(), whitespace.group(2).trim());
        }
        String normalized = TextNormalizer.normalizeLabel(line);
        return definitions.stream()
                .flatMap(definition -> labels(definition).stream().map(label -> Map.entry(label, definition)))
                .map(Map.Entry::getKey)
                .filter(label -> normalized.startsWith(label + " "))
                .max(Comparator.comparingInt(String::length))
                .map(label -> {
                    int numberStart = firstDigitIndex(line);
                    return numberStart > 0 ? new LineParts(line.substring(0, numberStart).trim(), line.substring(numberStart).trim()) : null;
                }).orElse(null);
    }

    private Optional<LabelMatch> matchLabel(String sourceLabel, List<KpiDefinition> definitions) {
        String normalized = TextNormalizer.normalizeLabel(sourceLabel);
        List<LabelMatch> matches = new ArrayList<>();
        for (KpiDefinition definition : definitions) {
            if (normalized.equals(TextNormalizer.normalizeLabel(definition.getDisplayName()))
                    || normalized.equals(TextNormalizer.normalizeLabel(definition.getCode()))) {
                matches.add(new LabelMatch(definition, 1.0, "EXACT"));
                continue;
            }
            if (definition.getAliases().stream().map(KpiAlias::getNormalizedAlias).anyMatch(normalized::equals)) {
                matches.add(new LabelMatch(definition, 0.98, "ALIAS"));
                continue;
            }
            double similarity = labels(definition).stream().mapToDouble(label -> similarity(normalized, label)).max().orElse(0);
            if (similarity >= properties.fuzzyThreshold()) {
                matches.add(new LabelMatch(definition, similarity * 0.90, "FUZZY"));
            }
        }
        return matches.stream().max(Comparator.comparingDouble(LabelMatch::score)
                .thenComparing(match -> match.definition().getCode(), Comparator.reverseOrder()));
    }

    private List<String> labels(KpiDefinition definition) {
        List<String> labels = new ArrayList<>();
        labels.add(TextNormalizer.normalizeLabel(definition.getCode()));
        labels.add(TextNormalizer.normalizeLabel(definition.getDisplayName()));
        labels.addAll(definition.getAliases().stream().map(KpiAlias::getNormalizedAlias).toList());
        return labels;
    }

    private NumericValue normalizeNumber(String raw) {
        String compact = raw.replace(" ", "");
        List<ParserWarning> warnings = new ArrayList<>();
        int comma = compact.lastIndexOf(',');
        int point = compact.lastIndexOf('.');
        if (comma >= 0 && point >= 0) {
            char decimal = comma > point ? ',' : '.';
            compact = compact.replace(decimal == ',' ? "." : ",", "").replace(decimal, '.');
        } else if (comma >= 0 || point >= 0) {
            int separator = Math.max(comma, point);
            int trailing = compact.length() - separator - 1;
            if (trailing == 3 && separator > 0) {
                compact = compact.substring(0, separator) + compact.substring(separator + 1);
                warnings.add(ParserWarning.warning("AMBIGUOUS_NUMBER", "A three-digit separator was interpreted as a thousands separator."));
            } else {
                compact = compact.replace(',', '.');
            }
        }
        return new NumericValue(new BigDecimal(compact), warnings);
    }

    private String capturedUnit(String valueText, int numberEnd) {
        String tail = valueText.substring(numberEnd).stripLeading();
        Matcher unit = Pattern.compile("^([%a-zA-Z°]+)").matcher(tail);
        return unit.find() ? unit.group(1) : null;
    }

    private void validatePlausibility(KpiDefinition definition, BigDecimal value, List<ParserWarning> warnings) {
        if ((definition.getPlausibleMin() != null && value.compareTo(definition.getPlausibleMin()) < 0)
                || (definition.getPlausibleMax() != null && value.compareTo(definition.getPlausibleMax()) > 0)) {
            warnings.add(ParserWarning.warning("OUTSIDE_PLAUSIBLE_RANGE", "The value is outside the configured plausible range."));
        }
    }

    private void validateUnit(KpiDefinition definition, String capturedUnit, List<ParserWarning> warnings) {
        if (capturedUnit != null && definition.getUnit() != null && !capturedUnit.equalsIgnoreCase(definition.getUnit())) {
            warnings.add(ParserWarning.warning("UNIT_MISMATCH", "The captured unit differs from the configured KPI unit."));
        }
    }

    private void markDuplicates(List<MutableCandidate> candidates) {
        Map<Long, Integer> counts = new HashMap<>();
        candidates.stream().filter(candidate -> candidate.definition != null)
                .forEach(candidate -> counts.merge(candidate.definition.getId(), 1, Integer::sum));
        candidates.stream().filter(candidate -> candidate.definition != null && counts.get(candidate.definition.getId()) > 1)
                .forEach(candidate -> candidate.warnings.add(ParserWarning.warning("DUPLICATE_KPI", "The KPI label occurs more than once in the input.")));
    }

    private double similarity(String left, String right) {
        if (left.equals(right)) return 1.0;
        int[] previous = new int[right.length() + 1];
        for (int j = 0; j <= right.length(); j++) previous[j] = j;
        for (int i = 1; i <= left.length(); i++) {
            int[] current = new int[right.length() + 1];
            current[0] = i;
            for (int j = 1; j <= right.length(); j++) {
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + (left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1));
            }
            previous = current;
        }
        return 1.0 - ((double) previous[right.length()] / Math.max(left.length(), right.length()));
    }

    private int firstDigitIndex(String line) {
        for (int i = 0; i < line.length(); i++) if (Character.isDigit(line.charAt(i))) return i;
        return -1;
    }

    private record LineParts(String label, String valueText) { }
    private record LabelMatch(KpiDefinition definition, double score, String kind) { }
    private record NumericValue(BigDecimal value, List<ParserWarning> warnings) { }

    private static final class MutableCandidate {
        private final KpiDefinition definition;
        private final String sourceLabel;
        private final String sourceLine;
        private final BigDecimal value;
        private final String capturedUnit;
        private final BigDecimal confidence;
        private final List<ParserWarning> warnings;

        private MutableCandidate(KpiDefinition definition, String sourceLabel, String sourceLine, BigDecimal value,
                                 String capturedUnit, BigDecimal confidence, List<ParserWarning> warnings) {
            this.definition = definition;
            this.sourceLabel = sourceLabel;
            this.sourceLine = sourceLine;
            this.value = value;
            this.capturedUnit = capturedUnit;
            this.confidence = confidence;
            this.warnings = warnings;
        }

        static MutableCandidate missing(LabelMatch match, String sourceLabel, String sourceLine) {
            return new MutableCandidate(match.definition(), sourceLabel, sourceLine, null, null,
                    BigDecimal.valueOf(match.score()).setScale(4, RoundingMode.HALF_UP),
                    new ArrayList<>(List.of(ParserWarning.warning("MISSING_VALUE", "The source explicitly contains no value."))));
        }

        static MutableCandidate invalid(LabelMatch match, String sourceLabel, String sourceLine) {
            return new MutableCandidate(match.definition(), sourceLabel, sourceLine, null, null,
                    BigDecimal.valueOf(match.score()).setScale(4, RoundingMode.HALF_UP),
                    new ArrayList<>(List.of(ParserWarning.warning("INVALID_NUMBER", "No numeric value could be extracted."))));
        }

        ParsedEntry toResponse(String id) {
            String level = confidence.compareTo(new BigDecimal("0.90")) >= 0 ? "HIGH"
                    : confidence.compareTo(new BigDecimal("0.70")) >= 0 ? "MEDIUM" : "LOW";
            return new ParsedEntry(id,
                    definition == null ? null : definition.getId(),
                    definition == null ? null : definition.getCode(),
                    definition == null ? null : definition.getDisplayName(),
                    sourceLabel, sourceLine, value, capturedUnit,
                    definition == null ? null : definition.getUnit(), confidence, level, List.copyOf(warnings));
        }
    }
}
