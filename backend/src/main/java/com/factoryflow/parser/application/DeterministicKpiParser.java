package com.factoryflow.parser.application;

import com.factoryflow.kpi.domain.KpiAlias;
import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.parser.api.AnalyzeReportResponse;
import com.factoryflow.parser.api.IgnoredSourceLine;
import com.factoryflow.parser.api.KpiSuggestion;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class DeterministicKpiParser {

    private static final Pattern EXPLICIT_SEPARATOR = Pattern.compile("\\s*(?:->|→|:|=)\\s*");
    private static final Pattern VALUE_START = Pattern.compile("^(.*?)(?:\\s+)(-{1,}|n/?a|vide|manquant|[+-]?\\d.*)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER = Pattern.compile("[-+]?\\d(?:[\\d ]*\\d)?(?:[.,]\\d+)?");
    private static final Pattern MISSING = Pattern.compile("^(?:-{1,}|n/?a|vide|manquant)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern WHATSAPP_METADATA = Pattern.compile(
            "^(?:\\[?\\d{1,2}:\\d{2}\\]?)(?:\\s+.*)?$|^(?:\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})(?:\\s+.*)?$"
    );
    private static final Set<String> HEADERS = Set.of(
            "kpi", "kpis", "indicateur", "indicateurs", "maintenance", "rapport kpi", "rapport maintenance"
    );

    private final ParserProperties properties;

    public DeterministicKpiParser(ParserProperties properties) {
        this.properties = properties;
    }

    public AnalyzeReportResponse parse(String rawText, AcquisitionSource source, List<KpiDefinition> definitions) {
        List<MutableCandidate> candidates = new ArrayList<>();
        List<UnrecognizedLine> unknown = new ArrayList<>();
        List<IgnoredSourceLine> ignored = new ArrayList<>();
        int unknownSequence = 1;
        int ignoredSequence = 1;

        String normalizedLineEndings = rawText.replace("\r\n", "\n").replace('\r', '\n');
        for (String originalLine : normalizedLineEndings.split("\n")) {
            String line = originalLine.strip();
            if (line.isEmpty()) continue;

            String normalizedLine = TextNormalizer.normalizeLabel(line);
            if (isHeader(normalizedLine)) {
                ignored.add(new IgnoredSourceLine("i" + ignoredSequence++, line, "HEADER"));
                continue;
            }
            if (WHATSAPP_METADATA.matcher(line).matches()) {
                ignored.add(new IgnoredSourceLine("i" + ignoredSequence++, line, "WHATSAPP_METADATA"));
                continue;
            }

            LineParts parts = splitLine(line, definitions);
            if (parts == null) {
                unknown.add(new UnrecognizedLine(
                        "u" + unknownSequence++, line, "NO_VALUE_SEPARATOR", null, suggestions(line, definitions)
                ));
                continue;
            }

            Optional<LabelMatch> labelMatch = matchLabel(parts.label(), definitions);
            String valueText = parts.valueText().trim();
            if (labelMatch.isPresent() && MISSING.matcher(valueText).matches()) {
                candidates.add(MutableCandidate.missing(labelMatch.get(), parts.label(), line));
                continue;
            }

            String multiValueSafe = valueText.replaceAll("(?<=\\d)-(?=\\d)", "|");
            Matcher numberMatcher = NUMBER.matcher(multiValueSafe);
            int valueIndex = 0;
            List<KpiSuggestion> labelSuggestions = suggestions(parts.label(), definitions);
            while (numberMatcher.find()) {
                NumericValue numeric = normalizeNumber(numberMatcher.group());
                String capturedUnit = capturedUnit(multiValueSafe, numberMatcher.end());
                if (valueIndex == 1 && "%".equals(capturedUnit) && labelMatch.isPresent()
                        && !candidates.isEmpty() && candidates.getLast().sourceLine.equals(line)) {
                    candidates.getLast().attachSecondary(numeric.value(), capturedUnit);
                    valueIndex++;
                    continue;
                }
                KpiDefinition definition = valueIndex == 0 ? labelMatch.map(LabelMatch::definition).orElse(null) : null;
                String matchMethod = valueIndex == 0 ? labelMatch.map(LabelMatch::kind).orElse("UNKNOWN") : "ADDITIONAL_VALUE";
                List<ParserWarning> warnings = new ArrayList<>(numeric.warnings());
                double confidence = valueIndex == 0 ? labelMatch.map(LabelMatch::score).orElse(0.0) : 0.50;

                if (definition == null) {
                    warnings.add(ParserWarning.warning(
                            valueIndex == 0 ? "UNKNOWN_KPI" : "ADDITIONAL_VALUE_REQUIRES_ASSIGNMENT",
                            valueIndex == 0
                                    ? "The source label is not mapped to a configured KPI."
                                    : "An additional value was found on the same line and requires KPI assignment during review."
                    ));
                } else {
                    validatePlausibility(definition, numeric.value(), warnings);
                    validateUnit(definition, capturedUnit, warnings);
                    if ("FUZZY_SUGGESTION".equals(matchMethod)) {
                        warnings.add(ParserWarning.warning("LOW_CONFIDENCE", "The KPI label was matched approximately and must be reviewed."));
                    }
                }
                candidates.add(new MutableCandidate(
                        definition,
                        parts.label(),
                        line,
                        numeric.value(),
                        capturedUnit,
                        BigDecimal.valueOf(confidence).setScale(4, RoundingMode.HALF_UP),
                        matchMethod,
                        warnings,
                        definition == null || "FUZZY_SUGGESTION".equals(matchMethod) ? labelSuggestions : List.of()
                ));
                valueIndex++;
            }

            if (valueIndex == 0) {
                if (labelMatch.isPresent()) {
                    candidates.add(MutableCandidate.invalid(labelMatch.get(), parts.label(), line));
                } else {
                    unknown.add(new UnrecognizedLine(
                            "u" + unknownSequence++, line, "NO_KPI_MATCH", parts.label(), labelSuggestions
                    ));
                }
            }
        }

        markDuplicates(candidates);
        List<ParsedEntry> entries = new ArrayList<>();
        int candidateSequence = 1;
        for (MutableCandidate candidate : candidates) {
            entries.add(candidate.toResponse("c" + candidateSequence++));
        }
        int recognized = (int) entries.stream().filter(entry -> entry.kpiDefinitionId() != null).count();
        int ready = (int) entries.stream().filter(entry -> "READY".equals(entry.reviewState())).count();
        int attention = (int) entries.stream().filter(entry -> "ATTENTION".equals(entry.reviewState())).count();
        int missing = (int) entries.stream().filter(entry -> "MISSING".equals(entry.reviewState())).count();
        int unresolvedEntries = (int) entries.stream().filter(entry -> "UNRESOLVED".equals(entry.reviewState())).count();
        int unresolved = unresolvedEntries + unknown.size();
        return new AnalyzeReportResponse(
                source,
                rawText,
                recognized,
                ready,
                attention,
                missing,
                unresolved,
                attention + unresolved,
                unknown.size(),
                List.copyOf(entries),
                List.copyOf(unknown),
                List.copyOf(ignored)
        );
    }

    private boolean isHeader(String normalized) {
        if (HEADERS.contains(normalized)) return true;
        return normalized.matches("^(?:kpi|indicateurs?)\\s*(?:du jour|maintenance|quotidiens?)?$");
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
                .flatMap(definition -> normalizedLabels(definition).stream())
                .filter(label -> normalized.startsWith(label + " "))
                .max(Comparator.comparingInt(String::length))
                .map(label -> {
                    int numberStart = firstDigitIndex(line);
                    return numberStart > 0 ? new LineParts(line.substring(0, numberStart).trim(), line.substring(numberStart).trim()) : null;
                })
                .orElse(null);
    }

    private Optional<LabelMatch> matchLabel(String sourceLabel, List<KpiDefinition> definitions) {
        String trimmed = sourceLabel.trim();
        List<LabelMatch> direct = new ArrayList<>();
        for (KpiDefinition definition : definitions) {
            if (trimmed.equalsIgnoreCase(definition.getDisplayName()) || trimmed.equalsIgnoreCase(definition.getCode())) {
                direct.add(new LabelMatch(definition, 1.0, "EXACT_CANONICAL"));
            }
        }
        if (uniqueDefinition(direct)) return Optional.of(direct.getFirst());

        List<LabelMatch> aliases = new ArrayList<>();
        for (KpiDefinition definition : definitions) {
            if (definition.getAliases().stream().map(KpiAlias::getAlias).anyMatch(trimmed::equalsIgnoreCase)) {
                aliases.add(new LabelMatch(definition, 0.99, "EXACT_ALIAS"));
            }
        }
        if (uniqueDefinition(aliases)) return Optional.of(aliases.getFirst());

        String normalized = TextNormalizer.normalizeLabel(sourceLabel);
        Map<Long, LabelMatch> normalizedMatches = new LinkedHashMap<>();
        for (KpiDefinition definition : definitions) {
            if (normalizedLabels(definition).contains(normalized)) {
                normalizedMatches.put(definition.getId(), new LabelMatch(definition, 0.97, "NORMALIZED"));
            }
        }
        if (normalizedMatches.size() == 1) return Optional.of(normalizedMatches.values().iterator().next());

        List<LabelMatch> fuzzy = fuzzyMatches(normalized, definitions);
        if (fuzzy.isEmpty() || fuzzy.getFirst().score() < properties.fuzzyThreshold()) return Optional.empty();
        if (fuzzy.size() > 1 && fuzzy.getFirst().score() - fuzzy.get(1).score() < properties.fuzzyAmbiguityMargin()) {
            return Optional.empty();
        }
        LabelMatch best = fuzzy.getFirst();
        return Optional.of(new LabelMatch(best.definition(), best.score() * 0.90, "FUZZY_SUGGESTION"));
    }

    private boolean uniqueDefinition(List<LabelMatch> matches) {
        return matches.stream().map(match -> match.definition().getId()).distinct().count() == 1 && !matches.isEmpty();
    }

    private List<LabelMatch> fuzzyMatches(String normalized, List<KpiDefinition> definitions) {
        return definitions.stream()
                .map(definition -> new LabelMatch(
                        definition,
                        normalizedLabels(definition).stream().mapToDouble(label -> similarity(normalized, label)).max().orElse(0),
                        "FUZZY_SUGGESTION"
                ))
                .sorted(Comparator.comparingDouble(LabelMatch::score).reversed()
                        .thenComparing(match -> match.definition().getCode()))
                .toList();
    }

    private List<KpiSuggestion> suggestions(String sourceLabel, List<KpiDefinition> definitions) {
        String normalized = TextNormalizer.normalizeLabel(sourceLabel);
        return fuzzyMatches(normalized, definitions).stream()
                .filter(match -> match.score() >= properties.suggestionThreshold())
                .limit(3)
                .map(match -> new KpiSuggestion(
                        match.definition().getId(),
                        match.definition().getCode(),
                        match.definition().getDisplayName(),
                        match.definition().getUnit(),
                        BigDecimal.valueOf(match.score()).setScale(4, RoundingMode.HALF_UP),
                        "FUZZY_SUGGESTION"
                ))
                .toList();
    }

    private List<String> normalizedLabels(KpiDefinition definition) {
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
                .forEach(candidate -> candidate.warnings.add(ParserWarning.warning("DUPLICATE_KPI", "The same canonical KPI occurs more than once in the input.")));
    }

    private double similarity(String left, String right) {
        if (left.equals(right)) return 1.0;
        if (left.isEmpty() || right.isEmpty()) return 0.0;
        int[] previous = new int[right.length() + 1];
        for (int j = 0; j <= right.length(); j++) previous[j] = j;
        for (int i = 1; i <= left.length(); i++) {
            int[] current = new int[right.length() + 1];
            current[0] = i;
            for (int j = 1; j <= right.length(); j++) {
                current[j] = Math.min(
                        Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + (left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1)
                );
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
        private final String matchMethod;
        private final List<ParserWarning> warnings;
        private final List<KpiSuggestion> suggestions;
        private BigDecimal secondaryValue;
        private String secondaryUnit;

        private MutableCandidate(
                KpiDefinition definition,
                String sourceLabel,
                String sourceLine,
                BigDecimal value,
                String capturedUnit,
                BigDecimal confidence,
                String matchMethod,
                List<ParserWarning> warnings,
                List<KpiSuggestion> suggestions
        ) {
            this.definition = definition;
            this.sourceLabel = sourceLabel;
            this.sourceLine = sourceLine;
            this.value = value;
            this.capturedUnit = capturedUnit;
            this.confidence = confidence;
            this.matchMethod = matchMethod;
            this.warnings = warnings;
            this.suggestions = suggestions;
        }

        static MutableCandidate missing(LabelMatch match, String sourceLabel, String sourceLine) {
            return new MutableCandidate(
                    match.definition(), sourceLabel, sourceLine, null, null,
                    BigDecimal.valueOf(match.score()).setScale(4, RoundingMode.HALF_UP),
                    match.kind(),
                    new ArrayList<>(List.of(ParserWarning.warning("MISSING_VALUE", "The source explicitly contains no value."))),
                    List.of()
            );
        }

        static MutableCandidate invalid(LabelMatch match, String sourceLabel, String sourceLine) {
            return new MutableCandidate(
                    match.definition(), sourceLabel, sourceLine, null, null,
                    BigDecimal.valueOf(match.score()).setScale(4, RoundingMode.HALF_UP),
                    match.kind(),
                    new ArrayList<>(List.of(ParserWarning.warning("INVALID_NUMBER", "No numeric value could be extracted."))),
                    List.of()
            );
        }

        void attachSecondary(BigDecimal value, String unit) {
            secondaryValue = value;
            secondaryUnit = unit;
        }

        ParsedEntry toResponse(String id) {
            String level = confidence.compareTo(new BigDecimal("0.90")) >= 0 ? "HIGH"
                    : confidence.compareTo(new BigDecimal("0.70")) >= 0 ? "MEDIUM" : "LOW";
            String reviewState;
            if (definition == null) reviewState = "UNRESOLVED";
            else if (warnings.stream().anyMatch(warning -> "MISSING_VALUE".equals(warning.code()))) reviewState = "MISSING";
            else if (warnings.isEmpty()) reviewState = "READY";
            else reviewState = "ATTENTION";
            return new ParsedEntry(
                    id,
                    definition == null ? null : definition.getId(),
                    definition == null ? null : definition.getCode(),
                    definition == null ? null : definition.getDisplayName(),
                    sourceLabel,
                    sourceLine,
                    value,
                    capturedUnit,
                    definition == null ? null : definition.getUnit(),
                    confidence,
                    level,
                    matchMethod,
                    reviewState,
                    List.copyOf(warnings),
                    List.copyOf(suggestions),
                    secondaryValue,
                    secondaryUnit
            );
        }
    }
}
