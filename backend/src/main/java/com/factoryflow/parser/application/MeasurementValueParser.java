package com.factoryflow.parser.application;

import com.factoryflow.parser.api.ParserWarning;
import com.factoryflow.shared.text.TextNormalizer;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class MeasurementValueParser {

    private static final Pattern MISSING = Pattern.compile(
            "^(?:-{1,}|n\\s*/?\\s*a|na|vide|manquant|non\\s+renseign[eé])$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern VALUE_WITH_OPTIONAL_UNIT = Pattern.compile(
            "^\\s*"
                    + "([+-]?\\d(?:[\\d\\s\\u00A0\\u202F.,]*\\d)?)"
                    + "\\s*"
                    + "([%°\\p{L}µμ][%°\\p{L}\\p{N}µμ/³²·._-]*)?"
                    + "\\s*$"
    );

    private final NumericInterpreter numericInterpreter;

    public MeasurementValueParser(
            NumericInterpreter numericInterpreter
    ) {
        this.numericInterpreter = numericInterpreter;
    }

    public MeasurementValue parse(
            String rawValueText,
            com.factoryflow.kpi.domain.KpiDefinition definition
    ) {
        if (rawValueText == null || rawValueText.isBlank()) {
            return invalid(
                    rawValueText,
                    "No numeric value could be extracted."
            );
        }

        String structural =
                TextNormalizer.normalizeStructuralText(rawValueText)
                        .trim();

        if (MISSING.matcher(structural).matches()) {
            return new MeasurementValue(
                    ValueState.MISSING,
                    structural,
                    null,
                    null,
                    null,
                    List.of(),
                    List.of(
                            ParserWarning.warning(
                                    "MISSING_VALUE",
                                    "The source explicitly contains no value."
                            )
                    )
            );
        }

        Matcher matcher =
                VALUE_WITH_OPTIONAL_UNIT.matcher(structural);

        if (!matcher.matches()) {
            return invalid(
                    structural,
                    "The value contains an unsupported or ambiguous measurement structure."
            );
        }

        String numericToken = matcher.group(1);
        String unit = normalizeUnit(matcher.group(2));

        NumericInterpreter.NumericInterpretation numeric =
                numericInterpreter.interpret(
                        numericToken,
                        definition
                );

        if (numeric.ambiguous()) {
            return new MeasurementValue(
                    ValueState.AMBIGUOUS,
                    structural,
                    numericToken,
                    numeric.value(),
                    unit,
                    numeric.alternatives(),
                    numeric.warnings()
            );
        }

        if (!numeric.valid() || numeric.value() == null) {
            return new MeasurementValue(
                    ValueState.INVALID,
                    structural,
                    numericToken,
                    null,
                    unit,
                    numeric.alternatives(),
                    numeric.warnings()
            );
        }

        return new MeasurementValue(
                ValueState.VALUE,
                structural,
                numericToken,
                numeric.value(),
                unit,
                numeric.alternatives(),
                numeric.warnings()
        );
    }

    private MeasurementValue invalid(
            String rawValue,
            String message
    ) {
        return new MeasurementValue(
                ValueState.INVALID,
                rawValue,
                null,
                null,
                null,
                List.of(),
                List.of(
                        ParserWarning.warning(
                                "INVALID_NUMBER",
                                message
                        )
                )
        );
    }

    private String normalizeUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            return null;
        }

        return TextNormalizer.normalizeWhitespace(unit);
    }

    public enum ValueState {
        VALUE,
        MISSING,
        AMBIGUOUS,
        INVALID
    }

    public record MeasurementValue(
            ValueState state,
            String rawValueText,
            String rawNumericToken,
            BigDecimal value,
            String capturedUnit,
            List<BigDecimal> alternatives,
            List<ParserWarning> warnings
    ) {
        public MeasurementValue {
            alternatives = alternatives == null
                    ? List.of()
                    : List.copyOf(alternatives);

            warnings = warnings == null
                    ? List.of()
                    : List.copyOf(warnings);
        }

        public boolean hasValue() {
            return state == ValueState.VALUE
                    && value != null;
        }

        public boolean missing() {
            return state == ValueState.MISSING;
        }

        public boolean needsReview() {
            return state == ValueState.AMBIGUOUS
                    || state == ValueState.INVALID
                    || !warnings.isEmpty();
        }
    }
}
