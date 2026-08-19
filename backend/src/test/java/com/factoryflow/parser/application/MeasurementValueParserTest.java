package com.factoryflow.parser.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.factoryflow.kpi.domain.KpiDefinition;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MeasurementValueParserTest {

    private MeasurementValueParser parser;

    @BeforeEach
    void setUp() {
        parser = new MeasurementValueParser(
                new NumericInterpreter()
        );
    }

    @Test
    void parsesDecimalWithAttachedUnit() {
        var result = parser.parse(
                "15,8t",
                definition("VRAC", "t")
        );

        assertThat(result.state())
                .isEqualTo(
                        MeasurementValueParser.ValueState.VALUE
                );

        assertThat(result.value())
                .isEqualByComparingTo("15.8");

        assertThat(result.capturedUnit())
                .isEqualTo("t");
    }

    @Test
    void parsesDecimalWithSeparatedUnit() {
        var result = parser.parse(
                "18,2 t",
                definition("SAC", "t")
        );

        assertThat(result.value())
                .isEqualByComparingTo("18.2");

        assertThat(result.capturedUnit())
                .isEqualTo("t");
    }

    @Test
    void parsesTemperatureUnit() {
        var result = parser.parse(
                "5°C",
                definition("DRYER", "°C")
        );

        assertThat(result.value())
                .isEqualByComparingTo("5");

        assertThat(result.capturedUnit())
                .isEqualTo("°C");
    }

    @Test
    void parsesPercentage() {
        var result = parser.parse(
                "77%",
                definition("LOAD", "%")
        );

        assertThat(result.value())
                .isEqualByComparingTo("77");

        assertThat(result.capturedUnit())
                .isEqualTo("%");
    }

    @Test
    void parsesValueWithoutUnit() {
        var result = parser.parse(
                "295456",
                definition("CHOLINE", null)
        );

        assertThat(result.value())
                .isEqualByComparingTo("295456");

        assertThat(result.capturedUnit())
                .isNull();
    }

    @Test
    void recognizesDashMissingMarker() {
        var result = parser.parse(
                "----",
                definition("VRAC", "t")
        );

        assertThat(result.missing()).isTrue();
        assertThat(result.value()).isNull();

        assertThat(result.warnings())
                .extracting("code")
                .containsExactly("MISSING_VALUE");
    }

    @Test
    void recognizesTextMissingMarkers() {
        for (String marker : List.of(
                "N/A",
                "NA",
                "vide",
                "manquant",
                "non renseigné",
                "non renseigne"
        )) {
            var result = parser.parse(
                    marker,
                    definition("VRAC", "t")
            );

            assertThat(result.missing())
                    .as(marker)
                    .isTrue();

            assertThat(result.value())
                    .as(marker)
                    .isNull();
        }
    }

    @Test
    void doesNotConvertMissingToZero() {
        var result = parser.parse(
                "---",
                definition("VRAC", "t")
        );

        assertThat(result.value()).isNull();
        assertThat(result.missing()).isTrue();
    }

    @Test
    void keepsExplicitZeroAsRealValue() {
        var result = parser.parse(
                "0",
                definition("VRAC", "t")
        );

        assertThat(result.state())
                .isEqualTo(
                        MeasurementValueParser.ValueState.VALUE
                );

        assertThat(result.value())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void preservesAmbiguousNumericInterpretation() {
        var result = parser.parse(
                "30.197",
                definitionWithRange(
                        "FUEL",
                        null,
                        BigDecimal.ZERO,
                        new BigDecimal("100000")
                )
        );

        assertThat(result.state())
                .isEqualTo(
                        MeasurementValueParser.ValueState.AMBIGUOUS
                );

        assertThat(result.value()).isNull();

        assertThat(result.alternatives())
                .hasSize(2);

        assertThat(result.warnings())
                .extracting("code")
                .contains("AMBIGUOUS_NUMBER");
    }

    @Test
    void allowsPlausibilityRangeToResolveAmbiguousNumber() {
        var result = parser.parse(
                "30.197",
                definitionWithRange(
                        "COUNTER",
                        null,
                        new BigDecimal("10000"),
                        new BigDecimal("100000")
                )
        );

        assertThat(result.state())
                .isEqualTo(
                        MeasurementValueParser.ValueState.VALUE
                );

        assertThat(result.value())
                .isEqualByComparingTo("30197");

        assertThat(result.warnings())
                .extracting("code")
                .contains(
                        "AMBIGUOUS_NUMBER_RESOLVED_BY_RANGE"
                );
    }

    @Test
    void rejectsMultipleValuesOutsideCompositeParser() {
        var result = parser.parse(
                "100 77%",
                definition("TOTAL", "t")
        );

        assertThat(result.state())
                .isEqualTo(
                        MeasurementValueParser.ValueState.INVALID
                );

        assertThat(result.value()).isNull();

        assertThat(result.warnings())
                .extracting("code")
                .contains("INVALID_NUMBER");
    }

    @Test
    void rejectsGarbageInsteadOfGuessing() {
        var result = parser.parse(
                "unavailable",
                definition("VRAC", "t")
        );

        assertThat(result.state())
                .isEqualTo(
                        MeasurementValueParser.ValueState.INVALID
                );

        assertThat(result.value()).isNull();
    }

    @Test
    void preservesRawNumericToken() {
        var result = parser.parse(
                "1 250 t",
                definition("TOTAL", "t")
        );

        assertThat(result.rawNumericToken())
                .isEqualTo("1 250");

        assertThat(result.value())
                .isEqualByComparingTo("1250");
    }

    private KpiDefinition definition(
            String code,
            String unit
    ) {
        return KpiDefinition.create(
                code,
                code,
                "Test",
                unit,
                null,
                null,
                true,
                List.of()
        );
    }

    private KpiDefinition definitionWithRange(
            String code,
            String unit,
            BigDecimal min,
            BigDecimal max
    ) {
        return KpiDefinition.create(
                code,
                code,
                "Test",
                unit,
                min,
                max,
                true,
                List.of()
        );
    }
}