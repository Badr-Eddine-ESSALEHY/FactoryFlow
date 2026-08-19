package com.factoryflow.parser.application;

import com.factoryflow.shared.text.TextNormalizer;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class LineSegmenter {

    private static final Pattern EXPLICIT_SEPARATOR =
            Pattern.compile("\\s*(?:->|→|:|=)\\s*");

    /*
     * Greedy label part is intentional.
     *
     * Compresseur 1 77108-77%
     * becomes:
     * label = "Compresseur 1"
     * value = "77108-77%"
     *
     * rather than incorrectly treating the "1" in the KPI name as the value.
     */
    private static final Pattern WHITESPACE_VALUE =
            Pattern.compile(
                    "^(.+\\S)\\s+"
                            + "((?:-{1,}|n\\s*/?\\s*a|na|vide|manquant|"
                            + "non\\s+renseign[eé]|indisponible|"
                            + "[+-]?\\d.*))$",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern STANDALONE_VALUE =
            Pattern.compile(
                    "^(?:"
                            + "-{1,}|"
                            + "n\\s*/?\\s*a|"
                            + "na|"
                            + "vide|"
                            + "manquant|"
                            + "non\\s+renseign[eé]|"
                            + "indisponible|"
                            + "[+-]?\\d(?:[\\d\\s\\u00A0\\u202F.,]*\\d)?"
                            + "(?:\\s*[%\\p{L}°]+)?"
                            + ")$",
                    Pattern.CASE_INSENSITIVE
            );

    public Optional<LineParts> segmentSingleLine(String sourceLine) {
        if (sourceLine == null || sourceLine.isBlank()) {
            return Optional.empty();
        }

        String line =
                TextNormalizer.normalizeStructuralText(sourceLine);

        Matcher explicit = EXPLICIT_SEPARATOR.matcher(line);

        if (explicit.find()) {
            String label =
                    line.substring(0, explicit.start()).trim();

            String value =
                    line.substring(explicit.end()).trim();

            if (label.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(
                    new LineParts(
                            label,
                            value,
                            line,
                            false
                    )
            );
        }

        Matcher whitespace = WHITESPACE_VALUE.matcher(line);

        if (whitespace.matches()) {
            return Optional.of(
                    new LineParts(
                            whitespace.group(1).trim(),
                            whitespace.group(2).trim(),
                            line,
                            false
                    )
            );
        }

        return Optional.empty();
    }

    /**
     * Supports deliberately conservative multiline KPI input:
     *
     * Vrac
     * 15,8 t
     *
     * A multiline merge is allowed only when:
     * - the first line is a recognized KPI label;
     * - the second line is a standalone value/missing marker;
     * - the KPI match itself is not ambiguous/unknown.
     */
    public Optional<LineParts> segmentMultiline(
            String possibleLabelLine,
            String possibleValueLine,
            KpiCatalogIndex catalog,
            KpiMatcher matcher
    ) {
        if (possibleLabelLine == null
                || possibleLabelLine.isBlank()
                || possibleValueLine == null
                || possibleValueLine.isBlank()) {
            return Optional.empty();
        }

        String labelLine =
                TextNormalizer.normalizeStructuralText(
                        possibleLabelLine
                );

        String valueLine =
                TextNormalizer.normalizeStructuralText(
                        possibleValueLine
                );

        /*
         * If the first line already contains a complete value structure,
         * it is not a multiline-label candidate.
         */
        if (segmentSingleLine(labelLine).isPresent()) {
            return Optional.empty();
        }

        if (!isStandaloneValue(valueLine)) {
            return Optional.empty();
        }

        KpiMatcher.MatchResult match =
                matcher.match(labelLine, catalog);

        if (!match.matched()) {
            return Optional.empty();
        }

        return Optional.of(
                new LineParts(
                        labelLine.trim(),
                        valueLine.trim(),
                        labelLine + "\n" + valueLine,
                        true
                )
        );
    }

    public boolean isStandaloneValue(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        return STANDALONE_VALUE.matcher(
                TextNormalizer.normalizeStructuralText(value).trim()
        ).matches();
    }

    public record LineParts(
            String label,
            String valueText,
            String sourceLine,
            boolean multiline
    ) {
    }
}