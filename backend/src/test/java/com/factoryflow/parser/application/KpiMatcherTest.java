package com.factoryflow.parser.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.factoryflow.kpi.domain.KpiDefinition;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KpiMatcherTest {

    private KpiMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new KpiMatcher(
                new ParserProperties(
                        0.82,
                        0.04,
                        0.45
                )
        );
    }

    @Test
    void exactCanonicalHasHighestAuthority() {
        KpiDefinition choline = definition(
                "CHOLINE",
                "Choline",
                List.of("Vitamine Choline")
        );

        KpiMatcher.MatchResult result =
                matcher.match(
                        "Choline",
                        catalog(choline)
                );

        assertThat(result.matched()).isTrue();
        assertThat(result.definition()).isSameAs(choline);
        assertThat(result.matchMethod())
                .isEqualTo("EXACT_CANONICAL");
        assertThat(result.score())
                .isEqualByComparingTo("1.0000");
        assertThat(result.requiresReview()).isFalse();
    }

    @Test
    void exactConfiguredAliasIsRecognizedBeforeFuzzyMatching() {
        KpiDefinition vrac = definition(
                "VRAC",
                "Vrac",
                List.of("Varc")
        );

        KpiMatcher.MatchResult result =
                matcher.match(
                        "Varc",
                        catalog(vrac)
                );

        assertThat(result.matched()).isTrue();
        assertThat(result.definition()).isSameAs(vrac);
        assertThat(result.matchMethod())
                .isEqualTo("EXACT_ALIAS");
        assertThat(result.requiresReview()).isFalse();
    }

    @Test
    void normalizedMatchingHandlesAccentAndPunctuationVariations() {
        KpiDefinition dryer = definition(
                "TEMPS_SECHEUR",
                "Temps sécheur",
                List.of()
        );

        KpiMatcher.MatchResult result =
                matcher.match(
                        "TEMPS-SÉCHEUR",
                        catalog(dryer)
                );

        assertThat(result.matched()).isTrue();
        assertThat(result.definition()).isSameAs(dryer);
        assertThat(result.matchMethod())
                .isEqualTo("NORMALIZED");
        assertThat(result.requiresReview()).isFalse();
    }

    @Test
    void compactMatchingHandlesSeparatedCharactersWithoutFuzzyAuthority() {
        KpiDefinition vrac = definition(
                "VRAC",
                "Vrac",
                List.of()
        );

        KpiMatcher.MatchResult result =
                matcher.match(
                        "V r a c",
                        catalog(vrac)
                );

        assertThat(result.matched()).isTrue();
        assertThat(result.definition()).isSameAs(vrac);
        assertThat(result.matchMethod())
                .isEqualTo("COMPACT");
        assertThat(result.requiresReview()).isFalse();
    }

    @Test
    void compactMatchingHandlesMissingSpaceBeforeKpiNumber() {
        KpiDefinition compressor = definition(
                "COMPRESSEUR_1",
                "Compresseur 1",
                List.of()
        );

        KpiMatcher.MatchResult result =
                matcher.match(
                        "Compresseur1",
                        catalog(compressor)
                );

        assertThat(result.matched()).isTrue();
        assertThat(result.definition())
                .isSameAs(compressor);
        assertThat(result.matchMethod())
                .isEqualTo("COMPACT");
    }

    @Test
    void ocrZeroForLetterOIsDetectedButRequiresReview() {
        KpiDefinition choline = definition(
                "CHOLINE",
                "Choline",
                List.of()
        );

        KpiMatcher.MatchResult result =
                matcher.match(
                        "Ch0line",
                        catalog(choline)
                );

        assertThat(result.matched()).isTrue();
        assertThat(result.definition()).isSameAs(choline);
        assertThat(result.matchMethod())
                .isEqualTo("OCR_NORMALIZED");
        assertThat(result.requiresReview()).isTrue();
        assertThat(result.suggestions()).isNotEmpty();
    }

    @Test
    void ocrLetterLCanMatchExpectedNumericOneButRequiresReview() {
        KpiDefinition compressor = definition(
                "COMPRESSEUR_1",
                "Compresseur 1",
                List.of()
        );

        KpiMatcher.MatchResult result =
                matcher.match(
                        "Compresseurl",
                        catalog(compressor)
                );

        assertThat(result.matched()).isTrue();
        assertThat(result.definition())
                .isSameAs(compressor);
        assertThat(result.matchMethod())
                .isEqualTo("OCR_NORMALIZED");
        assertThat(result.requiresReview()).isTrue();
    }

    @Test
    void strongUniqueTypoBecomesFuzzySuggestionRequiringReview() {
        KpiDefinition eurotech = definition(
                "EUROTECH_METER",
                "Compteur Eurotech",
                List.of()
        );

        KpiMatcher.MatchResult result =
                matcher.match(
                        "Compteur Erotech",
                        catalog(eurotech)
                );

        assertThat(result.matched()).isTrue();
        assertThat(result.definition())
                .isSameAs(eurotech);
        assertThat(result.matchMethod())
                .isEqualTo("FUZZY_SUGGESTION");
        assertThat(result.requiresReview()).isTrue();
        assertThat(result.suggestions())
                .isNotEmpty();
    }

    @Test
    void fuzzyCandidatesTooCloseRemainAmbiguous() {
        KpiDefinition first = definition(
                "COMPTEUR_A",
                "Compteur A",
                List.of()
        );

        KpiDefinition second = definition(
                "COMPTEUR_B",
                "Compteur B",
                List.of()
        );

        KpiMatcher.MatchResult result =
                matcher.match(
                        "Compteur C",
                        catalog(first, second)
                );

        assertThat(result.status())
                .isEqualTo(
                        KpiMatcher.MatchStatus.AMBIGUOUS
                );

        assertThat(result.definition()).isNull();
        assertThat(result.reason())
                .isEqualTo("FUZZY_AMBIGUOUS");

        assertThat(result.suggestions())
                .hasSize(2);
    }

    @Test
    void normalizedCollisionNeverSilentlyChoosesDefinition() {
        KpiDefinition first = definition(
                "FIRST",
                "Compteur Eau",
                List.of()
        );

        KpiDefinition second = definition(
                "SECOND",
                "Autre KPI",
                List.of("Compteur-Eau")
        );

        KpiMatcher.MatchResult result =
                matcher.match(
                        "Compteur Eau",
                        catalog(first, second)
                );

        /*
         * Exact canonical still legitimately wins because
         * "Compteur Eau" exactly names FIRST.
         */
        assertThat(result.matched()).isTrue();
        assertThat(result.definition()).isSameAs(first);
        assertThat(result.matchMethod())
                .isEqualTo("EXACT_CANONICAL");

        /*
         * But a punctuation variant no longer has exact canonical
         * authority and exposes the normalized collision.
         */
        KpiMatcher.MatchResult ambiguous =
                matcher.match(
                        "Compteur--Eau",
                        catalog(first, second)
                );

        assertThat(ambiguous.status())
                .isEqualTo(
                        KpiMatcher.MatchStatus.AMBIGUOUS
                );
        assertThat(ambiguous.definition()).isNull();
    }

    @Test
    void unrelatedLabelRemainsUnknownInsteadOfBeingForcedOntoCatalog() {
        KpiDefinition vrac = definition(
                "VRAC",
                "Vrac",
                List.of()
        );

        KpiDefinition choline = definition(
                "CHOLINE",
                "Choline",
                List.of()
        );

        KpiMatcher.MatchResult result =
                matcher.match(
                        "Pression hydraulique externe",
                        catalog(vrac, choline)
                );

        assertThat(result.status())
                .isEqualTo(KpiMatcher.MatchStatus.UNKNOWN);

        assertThat(result.definition()).isNull();
    }

    @Test
    void suggestionsAreLimitedAndSortedByRelevance() {
        KpiDefinition eurotech = definition(
                "EUROTECH",
                "Compteur Eurotech",
                List.of()
        );

        KpiDefinition water = definition(
                "WATER",
                "Compteur Eau",
                List.of()
        );

        KpiDefinition cicalim = definition(
                "CICALIM",
                "Compteur Cicalim",
                List.of()
        );

        KpiDefinition unrelated = definition(
                "VRAC",
                "Vrac",
                List.of()
        );

        List<com.factoryflow.parser.api.KpiSuggestion> suggestions =
                matcher.suggestions(
                        "Compteur Erotech",
                        catalog(
                                eurotech,
                                water,
                                cicalim,
                                unrelated
                        )
                );

        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions.size()).isLessThanOrEqualTo(3);

        assertThat(suggestions.getFirst().kpiCode())
                .isEqualTo("EUROTECH");
    }

    private KpiCatalogIndex catalog(
            KpiDefinition... definitions
    ) {
        return new KpiCatalogIndex(
                List.of(definitions)
        );
    }

    private KpiDefinition definition(
            String code,
            String name,
            List<String> aliases
    ) {
        return KpiDefinition.create(
                code,
                name,
                "Test",
                null,
                null,
                null,
                true,
                aliases
        );
    }
}