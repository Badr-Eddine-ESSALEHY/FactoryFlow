package com.factoryflow.parser.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.factoryflow.kpi.domain.KpiDefinition;
import java.util.List;
import org.junit.jupiter.api.Test;

class KpiCatalogIndexTest {

    @Test
    void indexesCanonicalCodeAndDisplayNameSeparatelyFromAliases() {
        KpiDefinition compressor = KpiDefinition.create(
                "COMPRESSEUR_1",
                "Compresseur 1",
                "Utilities",
                null,
                null,
                null,
                true,
                List.of("Compresseur principal")
        );

        KpiCatalogIndex index =
                new KpiCatalogIndex(List.of(compressor));

        assertThat(index.findExactCanonical("COMPRESSEUR_1"))
                .singleElement()
                .satisfies(match -> {
                    assertThat(match.definition()).isSameAs(compressor);
                    assertThat(match.kind())
                            .isEqualTo(KpiCatalogIndex.LabelKind.CODE);
                });

        assertThat(index.findExactCanonical("Compresseur 1"))
                .singleElement()
                .satisfies(match ->
                        assertThat(match.kind())
                                .isEqualTo(KpiCatalogIndex.LabelKind.DISPLAY_NAME)
                );

        assertThat(index.findExactAlias("Compresseur principal"))
                .singleElement()
                .satisfies(match ->
                        assertThat(match.kind())
                                .isEqualTo(KpiCatalogIndex.LabelKind.ALIAS)
                );
    }

    @Test
    void exactMatchingIsCaseInsensitiveButDoesNotUseDestructiveNormalization() {
        KpiDefinition vrac = KpiDefinition.create(
                "VRAC",
                "Vrac",
                "Production",
                "t",
                null,
                null,
                true,
                List.of()
        );

        KpiCatalogIndex index =
                new KpiCatalogIndex(List.of(vrac));

        assertThat(index.findExactCanonical("VRAC"))
                .isNotEmpty();

        assertThat(index.findExactCanonical("vrac"))
                .isNotEmpty();

        assertThat(index.findExactCanonical("V r a c"))
                .isEmpty();
    }

    @Test
    void normalizedLookupHandlesAccentsAndPunctuation() {
        KpiDefinition dryer = KpiDefinition.create(
                "TEMPS_SECHEUR",
                "Temps sécheur",
                "Utilities",
                "°C",
                null,
                null,
                true,
                List.of()
        );

        KpiCatalogIndex index =
                new KpiCatalogIndex(List.of(dryer));

        assertThat(index.findNormalized("Temps secheur"))
                .isNotEmpty();

        assertThat(index.findNormalized("TEMPS-SÉCHEUR"))
                .isNotEmpty();
    }

    @Test
    void compactLookupHandlesSpacingDifferencesWithoutFuzzyMatching() {
        KpiDefinition vrac = KpiDefinition.create(
                "VRAC",
                "Vrac",
                "Production",
                "t",
                null,
                null,
                true,
                List.of()
        );

        KpiDefinition compressor = KpiDefinition.create(
                "COMPRESSEUR_1",
                "Compresseur 1",
                "Utilities",
                null,
                null,
                null,
                true,
                List.of()
        );

        KpiCatalogIndex index =
                new KpiCatalogIndex(
                        List.of(vrac, compressor)
                );

        assertThat(index.findCompact("V r a c"))
                .anySatisfy(match ->
                        assertThat(match.definition())
                                .isSameAs(vrac)
                );

        assertThat(index.findCompact("Compresseur1"))
                .anySatisfy(match ->
                        assertThat(match.definition())
                                .isSameAs(compressor)
                );
    }

    @Test
    void preservesNormalizationCollisionsInsteadOfSilentlyChoosingOne() {
        KpiDefinition first = KpiDefinition.create(
                "FIRST",
                "Compteur Eau",
                "Test",
                null,
                null,
                null,
                true,
                List.of()
        );

        KpiDefinition second = KpiDefinition.create(
                "SECOND",
                "Autre KPI",
                "Test",
                null,
                null,
                null,
                true,
                List.of("Compteur-Eau")
        );

        KpiCatalogIndex index =
                new KpiCatalogIndex(List.of(first, second));

        assertThat(index.findNormalized("Compteur Eau"))
                .extracting(
                        variant ->
                                variant.definition().getCode()
                )
                .contains("FIRST", "SECOND");
    }

    @Test
    void exposesPrecomputedVariantsForLaterFuzzyRanking() {
        KpiDefinition vrac = KpiDefinition.create(
                "VRAC",
                "Vrac",
                "Production",
                "t",
                null,
                null,
                true,
                List.of("Varc")
        );

        KpiCatalogIndex index =
                new KpiCatalogIndex(List.of(vrac));

        assertThat(index.variants())
                .extracting(KpiCatalogIndex.LabelVariant::kind)
                .containsExactly(
                        KpiCatalogIndex.LabelKind.CODE,
                        KpiCatalogIndex.LabelKind.DISPLAY_NAME,
                        KpiCatalogIndex.LabelKind.ALIAS
                );
    }
}