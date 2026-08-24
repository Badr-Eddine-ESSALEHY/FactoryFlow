package com.factoryflow.parser.application;

import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.parser.api.AnalyzeReportResponse;
import com.factoryflow.parser.api.IgnoredSourceLine;
import com.factoryflow.parser.api.KpiSuggestion;
import com.factoryflow.parser.api.ParsedEntry;
import com.factoryflow.parser.api.ParserWarning;
import com.factoryflow.parser.api.UnrecognizedLine;
import com.factoryflow.report.domain.AcquisitionSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DeterministicKpiParser {

    private final SourceLineClassifier lineClassifier;
    private final LineSegmenter lineSegmenter;
    private final KpiMatcher kpiMatcher;
    private final MeasurementValueParser valueParser;
    private final CompositeMeasurementParser compositeParser;
    private final CandidateValidator candidateValidator;

    public DeterministicKpiParser(
            SourceLineClassifier lineClassifier,
            LineSegmenter lineSegmenter,
            KpiMatcher kpiMatcher,
            MeasurementValueParser valueParser,
            CompositeMeasurementParser compositeParser,
            CandidateValidator candidateValidator
    ) {
        this.lineClassifier = lineClassifier;
        this.lineSegmenter = lineSegmenter;
        this.kpiMatcher = kpiMatcher;
        this.valueParser = valueParser;
        this.compositeParser = compositeParser;
        this.candidateValidator = candidateValidator;
    }

    public AnalyzeReportResponse parse(
            String rawText,
            AcquisitionSource source,
            List<KpiDefinition> definitions
    ) {
        KpiCatalogIndex catalog =
                new KpiCatalogIndex(definitions);

        List<MutableCandidate> candidates =
                new ArrayList<>();

        List<UnrecognizedLine> unrecognized =
                new ArrayList<>();

        List<IgnoredSourceLine> ignored =
                new ArrayList<>();

        int unrecognizedSequence = 1;
        int ignoredSequence = 1;

        String normalizedLineEndings = rawText
                .replace("\r\n", "\n")
                .replace('\r', '\n');

        String[] lines =
                normalizedLineEndings.split("\n", -1);

        for (int index = 0; index < lines.length; index++) {
            String originalLine = lines[index];

            SourceLineClassifier.Classification classification =
                    lineClassifier.classify(originalLine);

            if (classification.type()
                    == SourceLineClassifier.LineType.EMPTY) {
                continue;
            }

            if (classification.ignored()) {
                ignored.add(
                        new IgnoredSourceLine(
                                "i" + ignoredSequence++,
                                originalLine.strip(),
                                classification.reason()
                        )
                );
                continue;
            }

            LineSegmenter.LineParts parts =
                    lineSegmenter.segmentSingleLine(originalLine)
                            .orElse(null);

            /*
             * Conservative multiline handling:
             *
             * Vrac
             * 15,8 t
             */
            if (parts == null && index + 1 < lines.length) {
                String nextLine = lines[index + 1];

                SourceLineClassifier.Classification nextClassification =
                        lineClassifier.classify(nextLine);

                if (nextClassification.type()
                        == SourceLineClassifier.LineType.CONTENT) {

                    parts = lineSegmenter.segmentMultiline(
                                    originalLine,
                                    nextLine,
                                    catalog,
                                    kpiMatcher
                            )
                            .orElse(null);

                    if (parts != null) {
                        index++;
                    }
                }
            }

            if (parts == null) {
                unrecognized.add(
                        new UnrecognizedLine(
                                "u" + unrecognizedSequence++,
                                originalLine.strip(),
                                "NO_VALUE_SEPARATOR",
                                null,
                                kpiMatcher.suggestions(
                                        originalLine,
                                        catalog
                                )
                        )
                );
                continue;
            }

            MutableCandidate candidate =
                    parseCandidate(parts, catalog);

            candidates.add(candidate);
        }

        markDuplicates(candidates);

        List<ParsedEntry> entries =
                new ArrayList<>();

        int candidateSequence = 1;

        for (MutableCandidate candidate : candidates) {
            entries.add(
                    candidate.toResponse(
                            "c" + candidateSequence++
                    )
            );
        }

        int recognized = (int) entries.stream()
                .filter(entry ->
                        entry.kpiDefinitionId() != null
                )
                .count();

        int ready = (int) entries.stream()
                .filter(entry ->
                        "READY".equals(entry.reviewState())
                )
                .count();

        int attention = (int) entries.stream()
                .filter(entry ->
                        "ATTENTION".equals(entry.reviewState())
                )
                .count();

        int missing = (int) entries.stream()
                .filter(entry ->
                        "MISSING".equals(entry.reviewState())
                )
                .count();

        int unresolvedEntries = (int) entries.stream()
                .filter(entry ->
                        "UNRESOLVED".equals(entry.reviewState())
                )
                .count();

        int unresolvedCount =
                unresolvedEntries + unrecognized.size();

        return new AnalyzeReportResponse(
                source,
                rawText,
                recognized,
                ready,
                attention,
                missing,
                unresolvedCount,
                attention + unresolvedCount,
                unrecognized.size(),
                List.copyOf(entries),
                List.copyOf(unrecognized),
                List.copyOf(ignored)
        );
    }

    private MutableCandidate parseCandidate(
            LineSegmenter.LineParts parts,
            KpiCatalogIndex catalog
    ) {
        KpiMatcher.MatchResult match =
                kpiMatcher.match(
                        parts.label(),
                        catalog
                );

        KpiDefinition definition =
                match.definition();

        List<ParserWarning> warnings =
                new ArrayList<>();

        List<KpiSuggestion> suggestions =
                new ArrayList<>(match.suggestions());

        addMatchWarnings(
                match,
                warnings
        );

        /*
         * Composite parsing is attempted only for KPI definitions
         * explicitly supporting composite semantics.
         */
        if (definition != null
                && compositeParser.supportsComposite(definition)) {

            var composite =
                    compositeParser.parse(
                            parts.valueText(),
                            definition
                    );

            if (composite.isPresent()) {
                CompositeMeasurementParser.CompositeMeasurement measurement =
                        composite.get();

                warnings.addAll(
                        measurement.warnings()
                );

                warnings.addAll(
                        candidateValidator.validate(
                                definition,
                                measurement.primaryValue(),
                                measurement.primaryUnit()
                        )
                );

                return new MutableCandidate(
                        definition,
                        parts.label(),
                        parts.sourceLine(),
                        measurement.primaryValue(),
                        measurement.primaryUnit(),
                        match.score(),
                        match.matchMethod(),
                        warnings,
                        suggestions,
                        measurement.secondaryValue(),
                        measurement.secondaryUnit()
                );
            }
        }

        MeasurementValueParser.MeasurementValue measurement =
                valueParser.parse(
                        parts.valueText(),
                        definition
                );

        warnings.addAll(
                measurement.warnings()
        );

        if (measurement.hasValue()) {
            warnings.addAll(
                    candidateValidator.validate(
                            definition,
                            measurement.value(),
                            measurement.capturedUnit()
                    )
            );
        }

        return new MutableCandidate(
                definition,
                parts.label(),
                parts.sourceLine(),
                measurement.value(),
                measurement.capturedUnit(),
                match.score(),
                match.matchMethod(),
                warnings,
                suggestions,
                null,
                null
        );
    }

    private void addMatchWarnings(
            KpiMatcher.MatchResult match,
            List<ParserWarning> warnings
    ) {
        if (match.status()
                == KpiMatcher.MatchStatus.UNKNOWN) {

            warnings.add(
                    ParserWarning.warning(
                            "UNKNOWN_KPI",
                            "The source label is not mapped to a configured KPI."
                    )
            );

            return;
        }

        if (match.status()
                == KpiMatcher.MatchStatus.AMBIGUOUS) {

            warnings.add(
                    ParserWarning.warning(
                            "AMBIGUOUS_KPI",
                            "The source label matches more than one KPI candidate and requires review."
                    )
            );

            return;
        }

        if (!match.requiresReview()) {
            return;
        }

        if ("FUZZY_SUGGESTION".equals(
                match.matchMethod()
        )) {
            warnings.add(
                    ParserWarning.warning(
                            "LOW_CONFIDENCE",
                            "The KPI label was matched approximately and must be reviewed."
                    )
            );

            return;
        }

        if ("OCR_NORMALIZED".equals(
                match.matchMethod()
        )) {
            warnings.add(
                    ParserWarning.warning(
                            "OCR_LABEL_CORRECTION",
                            "The KPI label required an OCR-style character correction and must be reviewed."
                    )
            );

            return;
        }

        warnings.add(
                ParserWarning.warning(
                        "MATCH_REQUIRES_REVIEW",
                        "The KPI match requires human verification."
                )
        );
    }

    private void markDuplicates(
            List<MutableCandidate> candidates
    ) {
        Map<Long, Integer> counts =
                new HashMap<>();

        /*
         * Unsaved unit-test definitions can have null IDs,
         * but production definitions loaded from PostgreSQL do not.
         */
        Map<String, Integer> codeCounts =
                new HashMap<>();

        for (MutableCandidate candidate : candidates) {
            if (candidate.definition == null) {
                continue;
            }

            if (candidate.definition.getId() != null) {
                counts.merge(
                        candidate.definition.getId(),
                        1,
                        Integer::sum
                );
            } else {
                codeCounts.merge(
                        candidate.definition.getCode(),
                        1,
                        Integer::sum
                );
            }
        }

        for (MutableCandidate candidate : candidates) {
            if (candidate.definition == null) {
                continue;
            }

            boolean duplicate;

            if (candidate.definition.getId() != null) {
                duplicate =
                        counts.getOrDefault(
                                candidate.definition.getId(),
                                0
                        ) > 1;
            } else {
                duplicate =
                        codeCounts.getOrDefault(
                                candidate.definition.getCode(),
                                0
                        ) > 1;
            }

            if (duplicate) {
                candidate.addWarningIfAbsent(
                        ParserWarning.warning(
                                "DUPLICATE_KPI",
                                "The same canonical KPI occurs more than once in the input."
                        )
                );
            }
        }
    }

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
        private final BigDecimal secondaryValue;
        private final String secondaryUnit;

        private MutableCandidate(
                KpiDefinition definition,
                String sourceLabel,
                String sourceLine,
                BigDecimal value,
                String capturedUnit,
                BigDecimal confidence,
                String matchMethod,
                List<ParserWarning> warnings,
                List<KpiSuggestion> suggestions,
                BigDecimal secondaryValue,
                String secondaryUnit
        ) {
            this.definition = definition;
            this.sourceLabel = sourceLabel;
            this.sourceLine = sourceLine;
            this.value = value;
            this.capturedUnit = capturedUnit;

            this.confidence =
                    confidence == null
                            ? BigDecimal.ZERO
                            : confidence;

            this.matchMethod =
                    matchMethod == null
                            ? "UNKNOWN"
                            : matchMethod;

            this.warnings =
                    new ArrayList<>(
                            warnings == null
                                    ? List.of()
                                    : warnings
                    );

            this.suggestions =
                    suggestions == null
                            ? List.of()
                            : List.copyOf(suggestions);

            this.secondaryValue = secondaryValue;
            this.secondaryUnit = secondaryUnit;
        }

        private void addWarningIfAbsent(
                ParserWarning warning
        ) {
            boolean alreadyPresent =
                    warnings.stream().anyMatch(
                            existing ->
                                    existing.code()
                                            .equals(warning.code())
                    );

            if (!alreadyPresent) {
                warnings.add(warning);
            }
        }

        private ParsedEntry toResponse(
                String candidateId
        ) {
            String confidenceLevel;

            if (confidence.compareTo(
                    new BigDecimal("0.90")
            ) >= 0) {
                confidenceLevel = "HIGH";
            } else if (confidence.compareTo(
                    new BigDecimal("0.70")
            ) >= 0) {
                confidenceLevel = "MEDIUM";
            } else {
                confidenceLevel = "LOW";
            }

            String reviewState;

            if (definition == null) {
                reviewState = "UNRESOLVED";
            } else if (hasWarning("MISSING_VALUE")) {
                reviewState = "MISSING";
            } else if (warnings.isEmpty()) {
                reviewState = "READY";
            } else {
                reviewState = "ATTENTION";
            }

            return new ParsedEntry(
                    candidateId,
                    definition == null
                            ? null
                            : definition.getId(),
                    definition == null
                            ? null
                            : definition.getCode(),
                    definition == null
                            ? null
                            : definition.getDisplayName(),
                    sourceLabel,
                    sourceLine,
                    value,
                    capturedUnit,
                    definition == null
                            ? null
                            : definition.getUnit(),
                    confidence,
                    confidenceLevel,
                    matchMethod,
                    reviewState,
                    List.copyOf(warnings),
                    suggestions,
                    secondaryValue,
                    secondaryUnit
            );
        }

        private boolean hasWarning(
                String code
        ) {
            return warnings.stream().anyMatch(
                    warning ->
                            code.equals(warning.code())
            );
        }
    }
}