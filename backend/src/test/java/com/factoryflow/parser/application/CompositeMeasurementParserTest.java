package com.factoryflow.parser.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.factoryflow.kpi.domain.KpiDefinition;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompositeMeasurementParserTest {

    private CompositeMeasurementParser parser;

    @BeforeEach
    void setUp() {
        parser = new CompositeMeasurementParser(
                new NumericInterpreter()
        );
    }

    @Test
    void parsesCompactCompressorComposite() {
        KpiDefinition compressor =
                compressor("COMPRESSEUR_1");

        CompositeMeasurementParser.CompositeMeasurement result =
                parser.parse(
                        "77108-77%",
                        compressor
                ).orElseThrow();

        assertThat(result.primaryValue())
                .isEqualByComparingTo("77108");

        assertThat(result.secondaryValue())
                .isEqualByComparingTo("77");

        assertThat(result.secondaryUnit())
                .isEqualTo("%");

        assertThat(result.valid()).isTrue();
        assertThat(result.warnings()).isEmpty();
    }

    @Test
    void parsesCompositeWithSpaces() {
        CompositeMeasurementParser.CompositeMeasurement result =
                parser.parse(
                        "77108 - 77 %",
                        compressor("COMPRESSEUR_1")
                ).orElseThrow();

        assertThat(result.primaryValue())
                .isEqualByComparingTo("77108");

        assertThat(result.secondaryValue())
                .isEqualByComparingTo("77");
    }

    @Test
    void parsesSlashComposite() {
        CompositeMeasurementParser.CompositeMeasurement result =
                parser.parse(
                        "77108 / 77%",
                        compressor("COMPRESSEUR_1")
                ).orElseThrow();

        assertThat(result.primaryValue())
                .isEqualByComparingTo("77108");

        assertThat(result.secondaryValue())
                .isEqualByComparingTo("77");
    }

    @Test
    void parsesPipeComposite() {
        CompositeMeasurementParser.CompositeMeasurement result =
                parser.parse(
                        "77108 | 77 %",
                        compressor("COMPRESSEUR_2")
                ).orElseThrow();

        assertThat(result.primaryValue())
                .isEqualByComparingTo("77108");

        assertThat(result.secondaryValue())
                .isEqualByComparingTo("77");
    }

    @Test
    void supportsAllConfiguredCompressorNumberCodes() {
        assertThat(
                parser.supportsComposite(
                        compressor("COMPRESSEUR_1")
                )
        ).isTrue();

        assertThat(
                parser.supportsComposite(
                        compressor("COMPRESSEUR_2")
                )
        ).isTrue();

        assertThat(
                parser.supportsComposite(
                        compressor("COMPRESSEUR_12")
                )
        ).isTrue();
    }

    @Test
    void doesNotTreatNormalKpiWithTwoNumbersAsComposite() {
        KpiDefinition production =
                KpiDefinition.create(
                        "TOTAL",
                        "Total",
                        "Production",
                        "t",
                        null,
                        null,
                        true,
                        List.of()
                );

        assertThat(
                parser.parse(
                        "100-77%",
                        production
                )
        ).isEmpty();
    }

    @Test
    void doesNotTreatArbitraryPercentageAsComposite() {
        assertThat(
                parser.parse(
                        "77%",
                        compressor("COMPRESSEUR_1")
                )
        ).isEmpty();
    }

    @Test
    void doesNotTreatSingleCounterAsComposite() {
        assertThat(
                parser.parse(
                        "77108",
                        compressor("COMPRESSEUR_1")
                )
        ).isEmpty();
    }

    @Test
    void flagsPercentageAboveOneHundred() {
        CompositeMeasurementParser.CompositeMeasurement result =
                parser.parse(
                        "77108-120%",
                        compressor("COMPRESSEUR_1")
                ).orElseThrow();

        assertThat(result.secondaryValue())
                .isEqualByComparingTo("120");

        assertThat(result.warnings())
                .extracting("code")
                .contains(
                        "SECONDARY_PERCENTAGE_OUT_OF_RANGE"
                );

        assertThat(result.needsReview()).isTrue();
    }

    @Test
    void flagsNegativePercentage() {
        CompositeMeasurementParser.CompositeMeasurement result =
                parser.parse(
                        "77108--5%",
                        compressor("COMPRESSEUR_1")
                ).orElseThrow();

        assertThat(result.secondaryValue())
                .isEqualByComparingTo("-5");

        assertThat(result.warnings())
                .extracting("code")
                .contains(
                        "SECONDARY_PERCENTAGE_OUT_OF_RANGE"
                );
    }

    @Test
    void preservesRawTokens() {
        CompositeMeasurementParser.CompositeMeasurement result =
                parser.parse(
                        "77 108 - 77 %",
                        compressor("COMPRESSEUR_1")
                ).orElseThrow();

        assertThat(result.rawPrimaryToken())
                .isEqualTo("77 108");

        assertThat(result.rawSecondaryToken())
                .isEqualTo("77");

        assertThat(result.primaryValue())
                .isEqualByComparingTo("77108");
    }

    @Test
    void primaryAmbiguousNotationDoesNotGetSilentlyAccepted() {
        KpiDefinition compressor =
                compressorWithWideRange(
                        "COMPRESSEUR_1"
                );

        CompositeMeasurementParser.CompositeMeasurement result =
                parser.parse(
                        "30.197-77%",
                        compressor
                ).orElseThrow();

        assertThat(result.primaryValue())
                .isNull();

        assertThat(result.warnings())
                .extracting("code")
                .contains(
                        "AMBIGUOUS_NUMBER",
                        "INVALID_COMPOSITE_PRIMARY"
                );

        assertThat(result.valid()).isFalse();
    }

    private KpiDefinition compressor(
            String code
    ) {
        return KpiDefinition.create(
                code,
                code.replace('_', ' '),
                "Utilities",
                null,
                null,
                null,
                true,
                List.of()
        );
    }

    private KpiDefinition compressorWithWideRange(
            String code
    ) {
        return KpiDefinition.create(
                code,
                code.replace('_', ' '),
                "Utilities",
                null,
                new java.math.BigDecimal("0"),
                new java.math.BigDecimal("100000"),
                true,
                List.of()
        );
    }
}