package com.factoryflow.parser.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.factoryflow.kpi.domain.KpiDefinition;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LineSegmenterTest {

    private LineSegmenter segmenter;
    private KpiMatcher matcher;

    @BeforeEach
    void setUp() {
        segmenter = new LineSegmenter();

        matcher = new KpiMatcher(
                new ParserProperties(
                        0.82,
                        0.04,
                        0.45
                )
        );
    }

    @Test
    void segmentsExplicitColon() {
        LineSegmenter.LineParts parts =
                segmenter.segmentSingleLine(
                        "Vrac : 15,8"
                ).orElseThrow();

        assertThat(parts.label())
                .isEqualTo("Vrac");

        assertThat(parts.valueText())
                .isEqualTo("15,8");

        assertThat(parts.multiline())
                .isFalse();
    }

    @Test
    void segmentsEqualsAndArrows() {
        assertThat(
                segmenter.segmentSingleLine(
                        "Sac = 18,2"
                ).orElseThrow().valueText()
        ).isEqualTo("18,2");

        assertThat(
                segmenter.segmentSingleLine(
                        "Choline -> 295456"
                ).orElseThrow().valueText()
        ).isEqualTo("295456");

        assertThat(
                segmenter.segmentSingleLine(
                        "Choline → 295456"
                ).orElseThrow().valueText()
        ).isEqualTo("295456");
    }

    @Test
    void segmentsWhitespaceSeparatedValue() {
        LineSegmenter.LineParts parts =
                segmenter.segmentSingleLine(
                        "Sac   18,2"
                ).orElseThrow();

        assertThat(parts.label())
                .isEqualTo("Sac");

        assertThat(parts.valueText())
                .isEqualTo("18,2");
    }

    @Test
    void keepsNumericSuffixInsideKpiLabel() {
        LineSegmenter.LineParts parts =
                segmenter.segmentSingleLine(
                        "Compresseur 1 77108-77%"
                ).orElseThrow();

        assertThat(parts.label())
                .isEqualTo("Compresseur 1");

        assertThat(parts.valueText())
                .isEqualTo("77108-77%");
    }

    @Test
    void keepsProductionLineNumberInsideLabel() {
        LineSegmenter.LineParts parts =
                segmenter.segmentSingleLine(
                        "Q. Produit P1 4402,4"
                ).orElseThrow();

        assertThat(parts.label())
                .isEqualTo("Q. Produit P1");

        assertThat(parts.valueText())
                .isEqualTo("4402,4");
    }

    @Test
    void segmentsUnknownMeasurementLikeLineForLaterResolution() {
        LineSegmenter.LineParts parts =
                segmenter.segmentSingleLine(
                        "Unexpected metric 44"
                ).orElseThrow();

        assertThat(parts.label())
                .isEqualTo("Unexpected metric");

        assertThat(parts.valueText())
                .isEqualTo("44");
    }

    @Test
    void preservesExplicitMissingValue() {
        LineSegmenter.LineParts parts =
                segmenter.segmentSingleLine(
                        "Vrac : ----"
                ).orElseThrow();

        assertThat(parts.label())
                .isEqualTo("Vrac");

        assertThat(parts.valueText())
                .isEqualTo("----");
    }

    @Test
    void preservesEmptyExplicitValueForLaterInvalidNumberHandling() {
        LineSegmenter.LineParts parts =
                segmenter.segmentSingleLine(
                        "Total :"
                ).orElseThrow();

        assertThat(parts.label())
                .isEqualTo("Total");

        assertThat(parts.valueText())
                .isEmpty();
    }

    @Test
    void recognizesStandaloneNumericValues() {
        assertThat(
                segmenter.isStandaloneValue("15,8 t")
        ).isTrue();

        assertThat(
                segmenter.isStandaloneValue("77108")
        ).isTrue();

        assertThat(
                segmenter.isStandaloneValue("77%")
        ).isTrue();

        assertThat(
                segmenter.isStandaloneValue("----")
        ).isTrue();

        assertThat(
                segmenter.isStandaloneValue("N/A")
        ).isTrue();
    }

    @Test
    void textSentenceIsNotStandaloneValue() {
        assertThat(
                segmenter.isStandaloneValue(
                        "Maintenance group"
                )
        ).isFalse();

        assertThat(
                segmenter.isStandaloneValue(
                        "Compteur eau"
                )
        ).isFalse();
    }

    @Test
    void combinesRecognizedKpiWithNextStandaloneValue() {
        KpiDefinition vrac =
                definition(
                        "VRAC",
                        "Vrac",
                        "t"
                );

        KpiCatalogIndex catalog =
                new KpiCatalogIndex(
                        List.of(vrac)
                );

        LineSegmenter.LineParts parts =
                segmenter.segmentMultiline(
                        "Vrac",
                        "15,8 t",
                        catalog,
                        matcher
                ).orElseThrow();

        assertThat(parts.label())
                .isEqualTo("Vrac");

        assertThat(parts.valueText())
                .isEqualTo("15,8 t");

        assertThat(parts.multiline())
                .isTrue();

        assertThat(parts.sourceLine())
                .isEqualTo("Vrac\n15,8 t");
    }

    @Test
    void combinesRecognizedKpiWithMissingMarker() {
        KpiDefinition vrac =
                definition(
                        "VRAC",
                        "Vrac",
                        "t"
                );

        KpiCatalogIndex catalog =
                new KpiCatalogIndex(
                        List.of(vrac)
                );

        LineSegmenter.LineParts parts =
                segmenter.segmentMultiline(
                        "Vrac",
                        "----",
                        catalog,
                        matcher
                ).orElseThrow();

        assertThat(parts.valueText())
                .isEqualTo("----");

        assertThat(parts.multiline())
                .isTrue();
    }

    @Test
    void doesNotBindUnknownLabelToNextNumber() {
        KpiDefinition vrac =
                definition(
                        "VRAC",
                        "Vrac",
                        "t"
                );

        KpiCatalogIndex catalog =
                new KpiCatalogIndex(
                        List.of(vrac)
                );

        assertThat(
                segmenter.segmentMultiline(
                        "Completely unrelated text",
                        "15,8",
                        catalog,
                        matcher
                )
        ).isEmpty();
    }

    @Test
    void doesNotBindKnownLabelToUnrelatedTextLine() {
        KpiDefinition vrac =
                definition(
                        "VRAC",
                        "Vrac",
                        "t"
                );

        KpiCatalogIndex catalog =
                new KpiCatalogIndex(
                        List.of(vrac)
                );

        assertThat(
                segmenter.segmentMultiline(
                        "Vrac",
                        "Maintenance group",
                        catalog,
                        matcher
                )
        ).isEmpty();
    }

    @Test
    void doesNotConvertAlreadyCompleteLineIntoMultilineObservation() {
        KpiDefinition vrac =
                definition(
                        "VRAC",
                        "Vrac",
                        "t"
                );

        KpiCatalogIndex catalog =
                new KpiCatalogIndex(
                        List.of(vrac)
                );

        assertThat(
                segmenter.segmentMultiline(
                        "Vrac : 15,8",
                        "20",
                        catalog,
                        matcher
                )
        ).isEmpty();
    }

    private KpiDefinition definition(
            String code,
            String name,
            String unit
    ) {
        return KpiDefinition.create(
                code,
                name,
                "Test",
                unit,
                null,
                null,
                true,
                List.of()
        );
    }
}