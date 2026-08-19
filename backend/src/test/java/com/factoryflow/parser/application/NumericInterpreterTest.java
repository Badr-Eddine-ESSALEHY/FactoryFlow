package com.factoryflow.parser.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.factoryflow.kpi.domain.KpiDefinition;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NumericInterpreterTest {

    private final NumericInterpreter interpreter = new NumericInterpreter();

    @ParameterizedTest(name = "{0}")
    @MethodSource("unambiguousNumbers")
    void interpretsUnambiguousIndustrialNumbers(
            String scenario,
            String raw,
            String expected
    ) {
        NumericInterpreter.NumericInterpretation result =
                interpreter.interpret(raw, null);

        assertThat(result.value()).isEqualByComparingTo(expected);
        assertThat(result.ambiguous()).isFalse();
        assertThat(result.warnings()).isEmpty();
    }

    static Stream<Arguments> unambiguousNumbers() {
        return Stream.of(
                Arguments.of("integer", "15", "15"),
                Arguments.of("decimal comma", "15,8", "15.8"),
                Arguments.of("decimal point", "15.8", "15.8"),
                Arguments.of("space thousands", "1 250", "1250"),
                Arguments.of("NBSP thousands", "1\u00A0250", "1250"),
                Arguments.of("narrow NBSP thousands", "1\u202F250", "1250"),
                Arguments.of("European mixed", "1.250,75", "1250.75"),
                Arguments.of("US mixed", "1,250.75", "1250.75"),
                Arguments.of("positive", "+15", "15"),
                Arguments.of("negative", "-15", "-15"),
                Arguments.of("leading zero decimal point", "0.125", "0.125"),
                Arguments.of("leading zero decimal comma", "0,125", "0.125"),
                Arguments.of("repeated point grouping", "1.234.567", "1234567"),
                Arguments.of("repeated comma grouping", "1,234,567", "1234567")
        );
    }

    @Test
    void leavesThreeDigitSingleSeparatorAmbiguousWithoutContext() {
        NumericInterpreter.NumericInterpretation result =
                interpreter.interpret("30.197", null);

        assertThat(result.value()).isNull();
        assertThat(result.ambiguous()).isTrue();

        assertThat(result.alternatives())
                .hasSize(2)
                .anySatisfy(value ->
                        assertThat(value).isEqualByComparingTo("30.197"))
                .anySatisfy(value ->
                        assertThat(value).isEqualByComparingTo("30197"));

        assertThat(result.warnings())
                .extracting("code")
                .containsExactly("AMBIGUOUS_NUMBER");
    }

    @Test
    void usesConfiguredPlausibleRangeToResolveAmbiguousNotation() {
        KpiDefinition definition = KpiDefinition.create(
                "TEST_COUNTER",
                "Test Counter",
                "Test",
                null,
                new BigDecimal("10000"),
                new BigDecimal("100000"),
                true,
                List.of()
        );

        NumericInterpreter.NumericInterpretation result =
                interpreter.interpret("30.197", definition);

        assertThat(result.value()).isEqualByComparingTo("30197");
        assertThat(result.ambiguous()).isFalse();

        assertThat(result.warnings())
                .extracting("code")
                .containsExactly("AMBIGUOUS_NUMBER_RESOLVED_BY_RANGE");
    }

    @Test
    void remainsAmbiguousWhenBothInterpretationsArePlausible() {
        KpiDefinition definition = KpiDefinition.create(
                "TEST_VALUE",
                "Test Value",
                "Test",
                null,
                BigDecimal.ZERO,
                new BigDecimal("100000"),
                true,
                List.of()
        );

        NumericInterpreter.NumericInterpretation result =
                interpreter.interpret("30.197", definition);

        assertThat(result.value()).isNull();
        assertThat(result.ambiguous()).isTrue();
        assertThat(result.alternatives()).hasSize(2);
    }

    @Test
    void rejectsMalformedThousandsGrouping() {
        NumericInterpreter.NumericInterpretation result =
                interpreter.interpret("12 34", null);

        assertThat(result.value()).isNull();
        assertThat(result.ambiguous()).isFalse();

        assertThat(result.warnings())
                .extracting("code")
                .containsExactly("INVALID_NUMBER");
    }

    @Test
    void rejectsAlphabeticGarbageInsteadOfGuessing() {
        NumericInterpreter.NumericInterpretation result =
                interpreter.interpret("12abc", null);

        assertThat(result.value()).isNull();

        assertThat(result.warnings())
                .extracting("code")
                .containsExactly("INVALID_NUMBER");
    }

    @Test
    void doesNotClampValuesToPlausibleRange() {
        KpiDefinition definition = KpiDefinition.create(
                "PRESSURE",
                "Pressure",
                "Test",
                "bar",
                BigDecimal.ZERO,
                new BigDecimal("100"),
                true,
                List.of()
        );

        NumericInterpreter.NumericInterpretation result =
                interpreter.interpret("150", definition);

        /*
         * Numeric interpretation itself preserves 150.
         * OUTSIDE_PLAUSIBLE_RANGE belongs to the later validation stage.
         */
        assertThat(result.value()).isEqualByComparingTo("150");
    }
}