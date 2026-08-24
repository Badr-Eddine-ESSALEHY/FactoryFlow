package com.factoryflow.parser.application;

import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.parser.api.KpiSuggestion;
import com.factoryflow.shared.text.TextNormalizer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class KpiMatcher {

    private static final BigDecimal EXACT_CANONICAL_SCORE =
            new BigDecimal("1.0000");

    private static final BigDecimal EXACT_ALIAS_SCORE =
            new BigDecimal("0.9900");

    private static final BigDecimal NORMALIZED_SCORE =
            new BigDecimal("0.9700");

    private static final BigDecimal COMPACT_SCORE =
            new BigDecimal("0.9500");

    /*
     * OCR normalization is useful but not authoritative.
     * It must remain below deterministic formatting matches and require review.
     */
    private static final BigDecimal OCR_NORMALIZED_SCORE =
            new BigDecimal("0.9200");

    private final ParserProperties properties;

    public KpiMatcher(ParserProperties properties) {
        this.properties = properties;
    }

    public MatchResult match(
            String sourceLabel,
            KpiCatalogIndex catalog
    ) {
        if (sourceLabel == null || sourceLabel.isBlank()) {
            return MatchResult.unknown(List.of());
        }

        String source = TextNormalizer.normalizeWhitespace(sourceLabel);

        MatchResult result = evaluateDeterministic(
                catalog.findExactCanonical(source),
                MatchMethod.EXACT_CANONICAL,
                EXACT_CANONICAL_SCORE,
                false
        );

        if (result != null) {
            return result;
        }

        result = evaluateDeterministic(
                catalog.findExactAlias(source),
                MatchMethod.EXACT_ALIAS,
                EXACT_ALIAS_SCORE,
                false
        );

        if (result != null) {
            return result;
        }

        result = evaluateDeterministic(
                catalog.findNormalized(source),
                MatchMethod.NORMALIZED,
                NORMALIZED_SCORE,
                false
        );

        if (result != null) {
            return result;
        }

        result = evaluateDeterministic(
                catalog.findCompact(source),
                MatchMethod.COMPACT,
                COMPACT_SCORE,
                false
        );

        if (result != null) {
            return result;
        }

        result = evaluateOcrAware(source, catalog);

        if (result != null) {
            return result;
        }

        return evaluateFuzzy(source, catalog);
    }

    public List<KpiSuggestion> suggestions(
            String sourceLabel,
            KpiCatalogIndex catalog
    ) {
        if (sourceLabel == null || sourceLabel.isBlank()) {
            return List.of();
        }

        return fuzzyRanking(sourceLabel, catalog).stream()
                .filter(candidate ->
                        candidate.score() >= properties.suggestionThreshold())
                .limit(3)
                .map(this::toSuggestion)
                .toList();
    }

    private MatchResult evaluateDeterministic(
            List<KpiCatalogIndex.LabelVariant> variants,
            MatchMethod method,
            BigDecimal score,
            boolean requiresReview
    ) {
        List<KpiDefinition> definitions =
                distinctDefinitions(variants);

        if (definitions.isEmpty()) {
            return null;
        }

        if (definitions.size() == 1) {
            return MatchResult.matched(
                    definitions.getFirst(),
                    score,
                    method.name(),
                    requiresReview,
                    List.of()
            );
        }

        return MatchResult.ambiguous(
                method.name() + "_AMBIGUOUS",
                deterministicSuggestions(
                        definitions,
                        score,
                        method.name()
                )
        );
    }

    private MatchResult evaluateOcrAware(
            String sourceLabel,
            KpiCatalogIndex catalog
    ) {
        String sourceCompact =
                TextNormalizer.compactLabel(sourceLabel);

        if (sourceCompact.isBlank() || !containsLetter(sourceCompact)) {
            return null;
        }

        String sourceOcr = ocrFold(sourceCompact);

        Map<String, KpiDefinition> matches =
                new LinkedHashMap<>();

        for (KpiCatalogIndex.LabelVariant variant : catalog.variants()) {
            if (variant.compact().isBlank()) {
                continue;
            }

            /*
             * OCR matching is considered only when the ordinary compact
             * representation did not already match.
             */
            if (variant.compact().equals(sourceCompact)) {
                continue;
            }

            if (ocrFold(variant.compact()).equals(sourceOcr)) {
                matches.putIfAbsent(
                        definitionKey(variant.definition()),
                        variant.definition()
                );
            }
        }

        if (matches.isEmpty()) {
            return null;
        }

        if (matches.size() == 1) {
            KpiDefinition definition =
                    matches.values().iterator().next();

            KpiSuggestion suggestion =
                    new KpiSuggestion(
                            definition.getId(),
                            definition.getCode(),
                            definition.getDisplayName(),
                            definition.getUnit(),
                            OCR_NORMALIZED_SCORE,
                            MatchMethod.OCR_NORMALIZED.name(),
                            suggestionStrength(OCR_NORMALIZED_SCORE.doubleValue())
                    );

            return MatchResult.matched(
                    definition,
                    OCR_NORMALIZED_SCORE,
                    MatchMethod.OCR_NORMALIZED.name(),
                    true,
                    List.of(suggestion)
            );
        }

        return MatchResult.ambiguous(
                "OCR_AMBIGUOUS",
                deterministicSuggestions(
                        List.copyOf(matches.values()),
                        OCR_NORMALIZED_SCORE,
                        MatchMethod.OCR_NORMALIZED.name()
                )
        );
    }

    private MatchResult evaluateFuzzy(
            String sourceLabel,
            KpiCatalogIndex catalog
    ) {
        List<RankedCandidate> ranked =
                fuzzyRanking(sourceLabel, catalog);

        if (ranked.isEmpty()) {
            return MatchResult.unknown(List.of());
        }

        List<KpiSuggestion> suggestions = ranked.stream()
                .filter(candidate ->
                        candidate.score() >= properties.suggestionThreshold())
                .limit(3)
                .map(this::toSuggestion)
                .toList();

        RankedCandidate best = ranked.getFirst();

        if (best.score() < properties.fuzzyThreshold()) {
            return MatchResult.unknown(suggestions);
        }

        if (ranked.size() > 1) {
            RankedCandidate second = ranked.get(1);

            if (second.score() >= properties.fuzzyThreshold()
                    && best.score() - second.score()
                    < properties.fuzzyAmbiguityMargin()) {

                return MatchResult.ambiguous(
                        "FUZZY_AMBIGUOUS",
                        suggestions
                );
            }
        }

        /*
         * This score describes parser-label similarity only.
         * It is NOT OCR confidence and is deliberately reduced because
         * fuzzy resolution always requires human review.
         */
        BigDecimal confidence = BigDecimal
                .valueOf(best.score() * 0.90)
                .setScale(4, RoundingMode.HALF_UP);

        return MatchResult.matched(
                best.definition(),
                confidence,
                MatchMethod.FUZZY_SUGGESTION.name(),
                true,
                suggestions
        );
    }

    private List<RankedCandidate> fuzzyRanking(
            String sourceLabel,
            KpiCatalogIndex catalog
    ) {
        String normalizedSource =
                TextNormalizer.normalizeLabel(sourceLabel);

        String compactSource =
                TextNormalizer.compactLabel(sourceLabel);

        if (normalizedSource.isBlank()) {
            return List.of();
        }

        Map<String, RankedCandidate> bestByDefinition =
                new LinkedHashMap<>();

        for (KpiCatalogIndex.LabelVariant variant : catalog.variants()) {
            double score = similarityScore(
                    normalizedSource,
                    compactSource,
                    variant.normalized(),
                    variant.compact()
            );

            String key = definitionKey(variant.definition());

            RankedCandidate previous =
                    bestByDefinition.get(key);

            RankedCandidate current =
                    new RankedCandidate(
                            variant.definition(),
                            score
                    );

            if (previous == null
                    || current.score() > previous.score()) {
                bestByDefinition.put(key, current);
            }
        }

        return bestByDefinition.values().stream()
                .sorted(
                        Comparator
                                .comparingDouble(
                                        RankedCandidate::score
                                )
                                .reversed()
                                .thenComparing(candidate ->
                                        candidate.definition().getCode())
                )
                .toList();
    }

    /**
     * Combines character similarity and token similarity.
     *
     * Character similarity handles small typing/OCR mistakes.
     * Token similarity helps multi-word industrial labels.
     */
    private double similarityScore(
            String sourceNormalized,
            String sourceCompact,
            String targetNormalized,
            String targetCompact
    ) {
        double normalizedCharacter =
                normalizedLevenshtein(
                        sourceNormalized,
                        targetNormalized
                );

        double compactCharacter =
                normalizedLevenshtein(
                        sourceCompact,
                        targetCompact
                );

        double token =
                tokenDiceSimilarity(
                        sourceNormalized,
                        targetNormalized
                );

        double structured =
                (normalizedCharacter * 0.85)
                        + (token * 0.15);

        return clamp(
                Math.max(
                        compactCharacter,
                        structured
                )
        );
    }

    private double normalizedLevenshtein(
            String left,
            String right
    ) {
        if (left.equals(right)) {
            return 1.0;
        }

        if (left.isEmpty() || right.isEmpty()) {
            return 0.0;
        }

        /*
         * Memory-efficient Levenshtein:
         * O(min(n,m)) auxiliary memory.
         */
        if (left.length() < right.length()) {
            String temporary = left;
            left = right;
            right = temporary;
        }

        int[] previous =
                new int[right.length() + 1];

        for (int j = 0; j <= right.length(); j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= left.length(); i++) {
            int[] current =
                    new int[right.length() + 1];

            current[0] = i;

            for (int j = 1; j <= right.length(); j++) {
                int substitution =
                        left.charAt(i - 1)
                                == right.charAt(j - 1)
                                ? 0
                                : 1;

                current[j] = Math.min(
                        Math.min(
                                current[j - 1] + 1,
                                previous[j] + 1
                        ),
                        previous[j - 1] + substitution
                );
            }

            previous = current;
        }

        int distance = previous[right.length()];
        int longest =
                Math.max(left.length(), right.length());

        return 1.0 - ((double) distance / longest);
    }

    private double tokenDiceSimilarity(
            String left,
            String right
    ) {
        Set<String> leftTokens = tokens(left);
        Set<String> rightTokens = tokens(right);

        if (leftTokens.isEmpty() || rightTokens.isEmpty()) {
            return 0.0;
        }

        int intersection = 0;

        for (String token : leftTokens) {
            if (rightTokens.contains(token)) {
                intersection++;
            }
        }

        return (2.0 * intersection)
                / (leftTokens.size() + rightTokens.size());
    }

    private Set<String> tokens(String normalized) {
        if (normalized == null || normalized.isBlank()) {
            return Set.of();
        }

        Set<String> result = new LinkedHashSet<>();

        for (String token : normalized.split("\\s+")) {
            if (!token.isBlank()) {
                result.add(token);
            }
        }

        return result;
    }

    /**
     * Conservative OCR fold.
     *
     * It is deliberately NOT used during canonical matching.
     * It is evaluated only after exact/normalized/compact matching failed.
     *
     * Supported high-value OCR confusions:
     *
     * 0 -> o
     * 1 -> l
     *
     * Examples:
     *
     * Ch0line      -> choline
     * Compresseurl -> comparable to Compresseur1
     */
    private String ocrFold(String value) {
        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replace('0', 'o')
                .replace('1', 'l');
    }

    private List<KpiDefinition> distinctDefinitions(
            List<KpiCatalogIndex.LabelVariant> variants
    ) {
        Map<String, KpiDefinition> result =
                new LinkedHashMap<>();

        for (KpiCatalogIndex.LabelVariant variant : variants) {
            result.putIfAbsent(
                    definitionKey(variant.definition()),
                    variant.definition()
            );
        }

        return List.copyOf(result.values());
    }

    private List<KpiSuggestion> deterministicSuggestions(
            List<KpiDefinition> definitions,
            BigDecimal score,
            String method
    ) {
        return definitions.stream()
                .sorted(Comparator.comparing(KpiDefinition::getCode))
                .limit(3)
                .map(definition ->
                        new KpiSuggestion(
                                definition.getId(),
                                definition.getCode(),
                                definition.getDisplayName(),
                                definition.getUnit(),
                                score,
                                method,
                                suggestionStrength(score.doubleValue())
                        )
                )
                .toList();
    }

    private KpiSuggestion toSuggestion(
            RankedCandidate candidate
    ) {
        return new KpiSuggestion(
                candidate.definition().getId(),
                candidate.definition().getCode(),
                candidate.definition().getDisplayName(),
                candidate.definition().getUnit(),
                BigDecimal.valueOf(candidate.score())
                        .setScale(4, RoundingMode.HALF_UP),
                MatchMethod.FUZZY_SUGGESTION.name(),
                suggestionStrength(candidate.score())
        );
    }

    private String suggestionStrength(double score) {
        return score >= properties.strongSuggestionThreshold() ? "STRONG" : "WEAK";
    }

    private String definitionKey(
            KpiDefinition definition
    ) {
        /*
         * Production KPI codes are unique.
         * Using code also keeps unit tests correct before JPA assigns IDs.
         */
        return definition.getCode()
                .toUpperCase(Locale.ROOT);
    }

    private boolean containsLetter(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isLetter(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private record RankedCandidate(
            KpiDefinition definition,
            double score
    ) {
    }

    public enum MatchStatus {
        MATCHED,
        AMBIGUOUS,
        UNKNOWN
    }

    public enum MatchMethod {
        EXACT_CANONICAL,
        EXACT_ALIAS,
        NORMALIZED,
        COMPACT,
        OCR_NORMALIZED,
        FUZZY_SUGGESTION
    }

    public record MatchResult(
            MatchStatus status,
            KpiDefinition definition,
            BigDecimal score,
            String matchMethod,
            boolean requiresReview,
            String reason,
            List<KpiSuggestion> suggestions
    ) {

        public MatchResult {
            suggestions = suggestions == null
                    ? List.of()
                    : List.copyOf(suggestions);
        }

        static MatchResult matched(
                KpiDefinition definition,
                BigDecimal score,
                String matchMethod,
                boolean requiresReview,
                List<KpiSuggestion> suggestions
        ) {
            return new MatchResult(
                    MatchStatus.MATCHED,
                    definition,
                    score,
                    matchMethod,
                    requiresReview,
                    null,
                    suggestions
            );
        }

        static MatchResult ambiguous(
                String reason,
                List<KpiSuggestion> suggestions
        ) {
            return new MatchResult(
                    MatchStatus.AMBIGUOUS,
                    null,
                    BigDecimal.ZERO,
                    "AMBIGUOUS",
                    true,
                    reason,
                    suggestions
            );
        }

        static MatchResult unknown(
                List<KpiSuggestion> suggestions
        ) {
            return new MatchResult(
                    MatchStatus.UNKNOWN,
                    null,
                    BigDecimal.ZERO,
                    "UNKNOWN",
                    true,
                    "NO_KPI_MATCH",
                    suggestions
            );
        }

        public boolean matched() {
            return status == MatchStatus.MATCHED
                    && definition != null;
        }
    }
}
