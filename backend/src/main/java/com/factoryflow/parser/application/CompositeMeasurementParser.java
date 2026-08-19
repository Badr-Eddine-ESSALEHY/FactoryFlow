package com.factoryflow.parser.application;

import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.parser.api.ParserWarning;
import com.factoryflow.shared.text.TextNormalizer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class CompositeMeasurementParser {

    private static final Pattern COMPRESSOR_CODE =
            Pattern.compile("^COMPRESSEUR_\\d+$", Pattern.CASE_INSENSITIVE);

    /*
     * Examples:
     *
     * 77108-77%
     * 77108 - 77 %
     * 77108/77%
     * 77108 / 77 %
     *
     * The separator belongs to the composite structure and is NOT
     * interpreted as a negative sign.
     */
    private static final Pattern COMPRESSOR_COMPOSITE =
            Pattern.compile(
                    "^\\s*"
                            + "([+-]?\\d(?:[\\d\\s\\u00A0\\u202F.,]*\\d)?)"
                            + "\\s*(?:-|/|\\|)\\s*"
                            + "([+-]?\\d(?:[\\d\\s\\u00A0\\u202F.,]*\\d)?)"
                            + "\\s*%\\s*$"
            );

    private final NumericInterpreter numericInterpreter;

    public CompositeMeasurementParser(
            NumericInterpreter numericInterpreter
    ) {
        this.numericInterpreter = numericInterpreter;
    }

    public Optional<CompositeMeasurement> parse(
            String rawValueText,
            KpiDefinition definition
    ) {
        if (rawValueText == null
                || rawValueText.isBlank()
                || definition == null
                || !supportsComposite(definition)) {
            return Optional.empty();
        }

        String structural =
                TextNormalizer.normalizeStructuralText(rawValueText);

        Matcher matcher =
                COMPRESSOR_COMPOSITE.matcher(structural);

        if (!matcher.matches()) {
            return Optional.empty();
        }

        String primaryToken = matcher.group(1);
        String secondaryToken = matcher.group(2);

        NumericInterpreter.NumericInterpretation primary =
                numericInterpreter.interpret(
                        primaryToken,
                        definition
                );

        /*
         * The secondary compressor measurement is a percentage.
         * It does not use the primary KPI plausible range because the
         * definition range describes the primary measurement.
         */
        NumericInterpreter.NumericInterpretation secondary =
                numericInterpreter.interpret(
                        secondaryToken,
                        null
                );

        List<ParserWarning> warnings =
                new ArrayList<>();

        warnings.addAll(primary.warnings());
        warnings.addAll(secondary.warnings());

        if (!primary.valid()
                || primary.ambiguous()
                || primary.value() == null) {
            warnings.add(ParserWarning.warning(
                    "INVALID_COMPOSITE_PRIMARY",
                    "The primary compressor measurement could not be interpreted safely."
            ));
        }

        if (!secondary.valid()
                || secondary.ambiguous()
                || secondary.value() == null) {
            warnings.add(ParserWarning.warning(
                    "INVALID_COMPOSITE_SECONDARY",
                    "The compressor percentage could not be interpreted safely."
            ));
        }

        BigDecimal secondaryValue =
                secondary.value();

        if (secondaryValue != null
                && (secondaryValue.compareTo(BigDecimal.ZERO) < 0
                || secondaryValue.compareTo(new BigDecimal("100")) > 0)) {
            warnings.add(ParserWarning.warning(
                    "SECONDARY_PERCENTAGE_OUT_OF_RANGE",
                    "The compressor percentage must normally be between 0 and 100."
            ));
        }

        return Optional.of(
                new CompositeMeasurement(
                        primaryToken,
                        primary.value(),
                        null,
                        secondaryToken,
                        secondaryValue,
                        "%",
                        List.copyOf(warnings)
                )
        );
    }

    public boolean supportsComposite(
            KpiDefinition definition
    ) {
        if (definition == null
                || definition.getCode() == null) {
            return false;
        }

        return COMPRESSOR_CODE.matcher(
                definition.getCode()
        ).matches();
    }

    public record CompositeMeasurement(
            String rawPrimaryToken,
            BigDecimal primaryValue,
            String primaryUnit,
            String rawSecondaryToken,
            BigDecimal secondaryValue,
            String secondaryUnit,
            List<ParserWarning> warnings
    ) {
        public CompositeMeasurement {
            warnings = warnings == null
                    ? List.of()
                    : List.copyOf(warnings);
        }

        public boolean valid() {
            return primaryValue != null
                    && secondaryValue != null
                    && warnings.stream().noneMatch(
                            warning ->
                                    warning.code().startsWith("INVALID_COMPOSITE")
                    );
        }

        public boolean needsReview() {
            return !warnings.isEmpty();
        }
    }
}