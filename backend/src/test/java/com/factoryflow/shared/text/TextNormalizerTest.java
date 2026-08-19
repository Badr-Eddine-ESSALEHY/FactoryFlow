package com.factoryflow.shared.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TextNormalizerTest {

    @Test
    void normalizesLabelsWithoutDependingOnCaseAccentsOrPunctuation() {
        assertThat(TextNormalizer.normalizeLabel("Durée  l'eau adous"))
                .isEqualTo("duree l eau adous");

        assertThat(TextNormalizer.normalizeLabel("COMPRESSEUR_1"))
                .isEqualTo("compresseur 1");

        assertThat(TextNormalizer.normalizeLabel("  Vrac : "))
                .isEqualTo("vrac");
    }

    @Test
    void compactLabelRemovesFormattingDifferences() {
        assertThat(TextNormalizer.compactLabel("V r a c"))
                .isEqualTo("vrac");

        assertThat(TextNormalizer.compactLabel("Compresseur 1"))
                .isEqualTo("compresseur1");

        assertThat(TextNormalizer.compactLabel("Compresseur1"))
                .isEqualTo("compresseur1");
    }

    @Test
    void structuralNormalizationPreservesParserSignificantCharacters() {
        assertThat(TextNormalizer.normalizeStructuralText("Vrac : 15,8 t"))
                .isEqualTo("Vrac : 15,8 t");

        assertThat(TextNormalizer.normalizeStructuralText("77108–77%"))
                .isEqualTo("77108-77%");

        assertThat(TextNormalizer.normalizeStructuralText("15.8 °C"))
                .isEqualTo("15.8 °C");
    }

    @Test
    void normalizesNonBreakingWhitespace() {
        String nbsp = "1\u00A0250";
        String narrowNbsp = "1\u202F250";

        assertThat(TextNormalizer.normalizeStructuralText(nbsp))
                .isEqualTo("1 250");

        assertThat(TextNormalizer.normalizeStructuralText(narrowNbsp))
                .isEqualTo("1 250");
    }

    @Test
    void removesDiacriticsWithoutDestroyingStructure() {
        assertThat(TextNormalizer.removeDiacritics("Température : 5°C"))
                .isEqualTo("Temperature : 5°C");
    }
}