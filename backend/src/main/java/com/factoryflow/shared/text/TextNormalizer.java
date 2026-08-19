package com.factoryflow.shared.text;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class TextNormalizer {

    private static final Pattern DIACRITIC_MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern LABEL_SEPARATORS = Pattern.compile("[^\\p{L}\\p{N}]+");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s\\u00A0\\u202F]+");

    private TextNormalizer() {
    }

    /**
     * Performs only safe structural normalization.
     *
     * This method deliberately preserves punctuation such as:
     * :, =, %, -, ., ,, ° and →
     *
     * because those characters can carry meaning during parsing.
     */
    public static String normalizeStructuralText(String value) {
        if (value == null) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .replace('\u00A0', ' ')
                .replace('\u202F', ' ')
                .replace('\u2010', '-')
                .replace('\u2011', '-')
                .replace('\u2012', '-')
                .replace('\u2013', '-')
                .replace('\u2014', '-')
                .replace('\u2212', '-')
                .strip();
    }

    /**
     * Produces the canonical representation used only for KPI label matching.
     *
     * Example:
     *
     * "Durée  l'eau adous" -> "duree l eau adous"
     * "COMPRESSEUR_1"     -> "compresseur 1"
     */
    public static String normalizeLabel(String value) {
        if (value == null) {
            return "";
        }

        String structural = normalizeStructuralText(value);

        String decomposed = Normalizer.normalize(structural, Normalizer.Form.NFD);
        String withoutMarks = DIACRITIC_MARKS.matcher(decomposed).replaceAll("");

        String lower = withoutMarks.toLowerCase(Locale.ROOT);
        String separated = LABEL_SEPARATORS.matcher(lower).replaceAll(" ");

        return WHITESPACE.matcher(separated)
                .replaceAll(" ")
                .trim();
    }

    /**
     * Removes spacing/punctuation differences after normal label
     * normalization.
     *
     * Used only as a secondary deterministic matching strategy.
     *
     * Examples:
     *
     * "V r a c"         -> "vrac"
     * "Compresseur 1"   -> "compresseur1"
     * "Compresseur1"    -> "compresseur1"
     */
    public static String compactLabel(String value) {
        return normalizeLabel(value).replace(" ", "");
    }

    /**
     * Normalizes whitespace without altering punctuation.
     */
    public static String normalizeWhitespace(String value) {
        if (value == null) {
            return "";
        }

        return WHITESPACE.matcher(normalizeStructuralText(value))
                .replaceAll(" ")
                .trim();
    }

    /**
     * Returns text without diacritical marks while otherwise preserving
     * characters and punctuation.
     */
    public static String removeDiacritics(String value) {
        if (value == null) {
            return "";
        }

        String decomposed = Normalizer.normalize(
                normalizeStructuralText(value),
                Normalizer.Form.NFD
        );

        return DIACRITIC_MARKS.matcher(decomposed).replaceAll("");
    }
}