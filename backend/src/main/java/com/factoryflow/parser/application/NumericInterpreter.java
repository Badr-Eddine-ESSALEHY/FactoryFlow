package com.factoryflow.parser.application;

import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.parser.api.ParserWarning;
import com.factoryflow.shared.text.TextNormalizer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class NumericInterpreter {

    private static final int MAX_NUMERIC_TOKEN_LENGTH = 128;

    private static final Pattern ALLOWED_NUMERIC_TOKEN =
            Pattern.compile("[+-]?[0-9][0-9.,\\s\\u00A0\\u202F]*");

    private static final Pattern SPACE_GROUPED_NUMBER =
            Pattern.compile(
                    "[+-]?\\d{1,3}(?:[ \\u00A0\\u202F]\\d{3})+(?:[.,]\\d+)?"
            );

    public NumericInterpretation interpret(String rawToken, KpiDefinition definition) {
        if (rawToken == null || rawToken.isBlank()) {
            return invalid("No numeric value was provided.");
        }

        String structural = TextNormalizer.normalizeStructuralText(rawToken).trim();

        if (structural.length() > MAX_NUMERIC_TOKEN_LENGTH) {
            return invalid("The numeric token is too long.");
        }

        if (!ALLOWED_NUMERIC_TOKEN.matcher(structural).matches()) {
            return invalid("The value contains characters that are not valid in a numeric token.");
        }

        if (containsWhitespace(structural)
                && !SPACE_GROUPED_NUMBER.matcher(structural).matches()) {
            return invalid("The spacing inside the number is not a valid thousands grouping.");
        }

        String compact = structural
                .replace(" ", "")
                .replace("\u00A0", "")
                .replace("\u202F", "");

        try {
            return interpretCompact(compact, definition);
        } catch (NumberFormatException exception) {
            return invalid("The numeric value could not be interpreted safely.");
        }
    }

    private NumericInterpretation interpretCompact(
            String compact,
            KpiDefinition definition
    ) {
        int commaCount = count(compact, ',');
        int pointCount = count(compact, '.');

        if (commaCount == 0 && pointCount == 0) {
            return resolved(
                    new BigDecimal(compact),
                    compact,
                    List.of()
            );
        }

        if (commaCount > 0 && pointCount > 0) {
            return interpretMixedSeparators(compact);
        }

        char separator = commaCount > 0 ? ',' : '.';
        int separatorCount = commaCount > 0 ? commaCount : pointCount;

        if (separatorCount > 1) {
            return interpretRepeatedSingleSeparator(compact, separator);
        }

        return interpretSingleSeparator(compact, separator, definition);
    }

    /**
     * Handles unambiguous mixed styles such as:
     *
     * 1.250,75
     * 1,250.75
     *
     * The last punctuation symbol is treated as the decimal separator and
     * the other punctuation symbol as grouping.
     */
    private NumericInterpretation interpretMixedSeparators(String compact) {
        int lastComma = compact.lastIndexOf(',');
        int lastPoint = compact.lastIndexOf('.');

        char decimalSeparator = lastComma > lastPoint ? ',' : '.';
        char groupingSeparator = decimalSeparator == ',' ? '.' : ',';

        String integerAndFraction = compact.replace(
                String.valueOf(groupingSeparator),
                ""
        );

        int decimalIndex = integerAndFraction.lastIndexOf(decimalSeparator);

        if (decimalIndex <= signOffset(integerAndFraction)
                || decimalIndex == integerAndFraction.length() - 1) {
            return invalid("The decimal separator is not positioned correctly.");
        }

        if (count(integerAndFraction, decimalSeparator) != 1) {
            return invalid("The number contains multiple decimal separators.");
        }

        String normalized = integerAndFraction.replace(decimalSeparator, '.');

        return resolved(
                new BigDecimal(normalized),
                normalized,
                List.of()
        );
    }

    /**
     * Handles:
     *
     * 1.234.567
     * 1,234,567
     *
     * Repeated identical separators are accepted only when they form valid
     * three-digit thousands groups.
     */
    private NumericInterpretation interpretRepeatedSingleSeparator(
            String compact,
            char separator
    ) {
        String unsigned = stripSign(compact);
        String[] groups = unsigned.split(
                Pattern.quote(String.valueOf(separator)),
                -1
        );

        if (groups.length < 2 || groups[0].isEmpty() || groups[0].length() > 3) {
            return invalid("The thousands grouping is malformed.");
        }

        for (int i = 1; i < groups.length; i++) {
            if (groups[i].length() != 3 || !digitsOnly(groups[i])) {
                return invalid("The thousands grouping is malformed.");
            }
        }

        if (!digitsOnly(groups[0])) {
            return invalid("The thousands grouping is malformed.");
        }

        String normalized = compact.replace(String.valueOf(separator), "");

        return resolved(
                new BigDecimal(normalized),
                normalized,
                List.of()
        );
    }

    /**
     * A single '.' or ',' is the difficult case.
     *
     * 15,8   -> decimal
     * 15.8   -> decimal
     *
     * 30.197 -> ambiguous:
     *           30.197 OR 30197
     *
     * Plausibility bounds may resolve the ambiguity. If they cannot, the
     * source's decimal reading remains editable while both interpretations
     * are exposed and explicit review is required.
     */
    private NumericInterpretation interpretSingleSeparator(
            String compact,
            char separator,
            KpiDefinition definition
    ) {
        int position = compact.indexOf(separator);

        if (position <= signOffset(compact)
                || position == compact.length() - 1) {
            return invalid("The numeric separator is not positioned correctly.");
        }

        String unsigned = stripSign(compact);
        int unsignedPosition = unsigned.indexOf(separator);

        String integerPart = unsigned.substring(0, unsignedPosition);
        String fractionalPart = unsigned.substring(unsignedPosition + 1);

        if (!digitsOnly(integerPart) || !digitsOnly(fractionalPart)) {
            return invalid("The numeric value is malformed.");
        }

        /*
         * 0.125 / 0,125 is overwhelmingly a decimal representation.
         * Treating it as 125 would create an artificial thousands meaning.
         */
        if ("0".equals(integerPart)) {
            return decimal(compact, separator);
        }

        int trailingDigits = fractionalPart.length();

        if (trailingDigits != 3) {
            return decimal(compact, separator);
        }

        BigDecimal decimalCandidate =
                new BigDecimal(compact.replace(separator, '.'));

        BigDecimal groupedCandidate =
                new BigDecimal(compact.replace(String.valueOf(separator), ""));

        boolean decimalPlausible = isPlausible(definition, decimalCandidate);
        boolean groupedPlausible = isPlausible(definition, groupedCandidate);

        if (hasPlausibilityInformation(definition)
                && decimalPlausible != groupedPlausible) {

            BigDecimal selected =
                    decimalPlausible ? decimalCandidate : groupedCandidate;

            return new NumericInterpretation(
                    selected,
                    selected.toPlainString(),
                    false,
                    List.of(decimalCandidate, groupedCandidate),
                    List.of(ParserWarning.warning(
                            "AMBIGUOUS_NUMBER_RESOLVED_BY_RANGE",
                            "The number had two possible interpretations; "
                                    + "the configured KPI plausible range selected one interpretation."
                    ))
            );
        }

        return new NumericInterpretation(
                decimalCandidate,
                decimalCandidate.toPlainString(),
                true,
                distinctCandidates(decimalCandidate, groupedCandidate),
                List.of(ParserWarning.warning(
                        "AMBIGUOUS_NUMBER",
                        "The number can represent either a decimal value "
                                + "or a thousands-grouped integer and requires review."
                ))
        );
    }

    private NumericInterpretation decimal(String compact, char separator) {
        String normalized = compact.replace(separator, '.');

        return resolved(
                new BigDecimal(normalized),
                normalized,
                List.of()
        );
    }

    private boolean hasPlausibilityInformation(KpiDefinition definition) {
        return definition != null
                && (definition.getPlausibleMin() != null
                || definition.getPlausibleMax() != null);
    }

    private boolean isPlausible(
            KpiDefinition definition,
            BigDecimal value
    ) {
        if (definition == null) {
            return true;
        }

        if (definition.getPlausibleMin() != null
                && value.compareTo(definition.getPlausibleMin()) < 0) {
            return false;
        }

        if (definition.getPlausibleMax() != null
                && value.compareTo(definition.getPlausibleMax()) > 0) {
            return false;
        }

        return true;
    }

    private NumericInterpretation resolved(
            BigDecimal value,
            String normalizedToken,
            List<ParserWarning> warnings
    ) {
        return new NumericInterpretation(
                value,
                normalizedToken,
                false,
                List.of(value),
                List.copyOf(warnings)
        );
    }

    private NumericInterpretation invalid(String message) {
        return new NumericInterpretation(
                null,
                null,
                false,
                List.of(),
                List.of(ParserWarning.warning(
                        "INVALID_NUMBER",
                        message
                ))
        );
    }

    private List<BigDecimal> distinctCandidates(
            BigDecimal first,
            BigDecimal second
    ) {
        if (first.compareTo(second) == 0) {
            return List.of(first);
        }
        return List.of(first, second);
    }

    private int count(String value, char character) {
        int result = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == character) {
                result++;
            }
        }
        return result;
    }

    private boolean containsWhitespace(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i))
                    || value.charAt(i) == '\u00A0'
                    || value.charAt(i) == '\u202F') {
                return true;
            }
        }
        return false;
    }

    private boolean digitsOnly(String value) {
        if (value.isEmpty()) {
            return false;
        }

        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private String stripSign(String value) {
        if (value.startsWith("+") || value.startsWith("-")) {
            return value.substring(1);
        }
        return value;
    }

    private int signOffset(String value) {
        return value.startsWith("+") || value.startsWith("-") ? 0 : -1;
    }

    public record NumericInterpretation(
            BigDecimal value,
            String normalizedToken,
            boolean ambiguous,
            List<BigDecimal> alternatives,
            List<ParserWarning> warnings
    ) {
        public NumericInterpretation {
            alternatives = alternatives == null
                    ? List.of()
                    : List.copyOf(alternatives);

            warnings = warnings == null
                    ? List.of()
                    : List.copyOf(warnings);
        }

        public boolean valid() {
            return value != null || ambiguous;
        }
    }
}
