package com.factoryflow.parser.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.factoryflow.kpi.domain.KpiDefinition;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CandidateValidatorTest {

    private CandidateValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CandidateValidator();
    }

    @Test
    void acceptsValueInsidePlausibleRange() {
        var warnings = validator.validate(
                definition(
                        "PRESSURE",
                        "bar",
                        "0",
                        "100"
                ),
                new BigDecimal("50"),
                "bar"
        );

        assertThat(warnings).isEmpty();
    }

    @Test
    void warnsBelowPlausibleMinimum() {
        var warnings = validator.validate(
                definition(
                        "PRESSURE",
                        "bar",
                        "10",
                        "100"
                ),
                new BigDecimal("5"),
                "bar"
        );

        assertThat(warnings)
                .extracting("code")
                .containsExactly(
                        "OUTSIDE_PLAUSIBLE_RANGE"
                );
    }

    @Test
    void warnsAbovePlausibleMaximum() {
        var warnings = validator.validate(
                definition(
                        "PRESSURE",
                        "bar",
                        "0",
                        "100"
                ),
                new BigDecimal("150"),
                "bar"
        );

        assertThat(warnings)
                .extracting("code")
                .containsExactly(
                        "OUTSIDE_PLAUSIBLE_RANGE"
                );
    }

    @Test
    void neverChangesOriginalValue() {
        BigDecimal value =
                new BigDecimal("150");

        validator.validate(
                definition(
                        "PRESSURE",
                        "bar",
                        "0",
                        "100"
                ),
                value,
                "bar"
        );

        assertThat(value)
                .isEqualByComparingTo("150");
    }

    @Test
    void warnsOnUnitMismatch() {
        var warnings = validator.validate(
                definition(
                        "VRAC",
                        "t",
                        null,
                        null
                ),
                new BigDecimal("15.8"),
                "kg"
        );

        assertThat(warnings)
                .extracting("code")
                .containsExactly("UNIT_MISMATCH");
    }

    @Test
    void unitComparisonIsCaseInsensitive() {
        var warnings = validator.validate(
                definition(
                        "TEMP",
                        "°C",
                        null,
                        null
                ),
                new BigDecimal("5"),
                "°c"
        );

        assertThat(warnings).isEmpty();
    }

    @Test
    void missingCapturedUnitDoesNotInventMismatch() {
        var warnings = validator.validate(
                definition(
                        "VRAC",
                        "t",
                        null,
                        null
                ),
                new BigDecimal("15.8"),
                null
        );

        assertThat(warnings).isEmpty();
    }

    @Test
    void nullValueDoesNotCreateRangeWarning() {
        var warnings = validator.validate(
                definition(
                        "VRAC",
                        "t",
                        "0",
                        "100"
                ),
                null,
                "t"
        );

        assertThat(warnings).isEmpty();
    }

    @Test
    void unknownDefinitionDoesNotProduceValidationWarnings() {
        assertThat(
                validator.validate(
                        null,
                        new BigDecimal("999"),
                        "whatever"
                )
        ).isEmpty();
    }

    private KpiDefinition definition(
            String code,
            String unit,
            String min,
            String max
    ) {
        return KpiDefinition.create(
                code,
                code,
                "Test",
                unit,
                min == null ? null : new BigDecimal(min),
                max == null ? null : new BigDecimal(max),
                true,
                List.of()
        );
    }
}