package com.factoryflow.parser.application;

import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.parser.api.ParserWarning;
import com.factoryflow.shared.text.TextNormalizer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class CandidateValidator {

    public List<ParserWarning> validate(
            KpiDefinition definition,
            BigDecimal value,
            String capturedUnit
    ) {
        if (definition == null) {
            return List.of();
        }

        List<ParserWarning> warnings =
                new ArrayList<>();

        validatePlausibility(
                definition,
                value,
                warnings
        );

        validateUnit(
                definition,
                capturedUnit,
                warnings
        );

        return List.copyOf(warnings);
    }

    private void validatePlausibility(
            KpiDefinition definition,
            BigDecimal value,
            List<ParserWarning> warnings
    ) {
        if (value == null) {
            return;
        }

        if (definition.getPlausibleMin() != null
                && value.compareTo(
                        definition.getPlausibleMin()
                ) < 0) {

            warnings.add(
                    ParserWarning.warning(
                            "OUTSIDE_PLAUSIBLE_RANGE",
                            "The value is below the configured plausible range."
                    )
            );

            return;
        }

        if (definition.getPlausibleMax() != null
                && value.compareTo(
                        definition.getPlausibleMax()
                ) > 0) {

            warnings.add(
                    ParserWarning.warning(
                            "OUTSIDE_PLAUSIBLE_RANGE",
                            "The value is above the configured plausible range."
                    )
            );
        }
    }

    private void validateUnit(
            KpiDefinition definition,
            String capturedUnit,
            List<ParserWarning> warnings
    ) {
        String expectedUnit =
                definition.getUnit();

        if (capturedUnit == null
                || capturedUnit.isBlank()
                || expectedUnit == null
                || expectedUnit.isBlank()) {
            return;
        }

        if (!normalizeUnit(capturedUnit)
                .equals(normalizeUnit(expectedUnit))) {

            warnings.add(
                    ParserWarning.warning(
                            "UNIT_MISMATCH",
                            "The captured unit differs from the configured KPI unit."
                    )
            );
        }
    }

    private String normalizeUnit(String unit) {
        return TextNormalizer.normalizeWhitespace(unit)
                .replace('μ', 'µ')
                .toLowerCase(Locale.ROOT);
    }
}