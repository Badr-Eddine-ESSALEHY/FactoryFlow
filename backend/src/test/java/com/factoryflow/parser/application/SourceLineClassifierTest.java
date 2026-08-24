package com.factoryflow.parser.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SourceLineClassifierTest {

    private SourceLineClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new SourceLineClassifier();
    }

    @Test
    void classifiesBlankLinesAsEmpty() {
        assertThat(classifier.classify("").type())
                .isEqualTo(SourceLineClassifier.LineType.EMPTY);

        assertThat(classifier.classify("   ").type())
                .isEqualTo(SourceLineClassifier.LineType.EMPTY);
    }

    @Test
    void recognizesKnownHeaders() {
        assertThat(classifier.classify("KPI :").type())
                .isEqualTo(SourceLineClassifier.LineType.HEADER);

        assertThat(classifier.classify("Rapport maintenance").type())
                .isEqualTo(SourceLineClassifier.LineType.HEADER);

        assertThat(classifier.classify("Indicateurs du jour").type())
                .isEqualTo(SourceLineClassifier.LineType.HEADER);
    }

    @Test
    void recognizesStandaloneWhatsappTimes() {
        assertThat(classifier.classify("10:52").type())
                .isEqualTo(SourceLineClassifier.LineType.WHATSAPP_METADATA);

        assertThat(classifier.classify("[10:52]").type())
                .isEqualTo(SourceLineClassifier.LineType.WHATSAPP_METADATA);
    }

    @Test
    void recognizesWhatsappTimePrefixedMetadata() {
        assertThat(classifier.classify("[10:52] Maintenance group").type())
                .isEqualTo(SourceLineClassifier.LineType.WHATSAPP_METADATA);

        assertThat(classifier.classify("10:52 Ahmed").type())
                .isEqualTo(SourceLineClassifier.LineType.WHATSAPP_METADATA);
    }

    @Test
    void recognizesCommonDateMetadata() {
        assertThat(classifier.classify("13/08/2026").type())
                .isEqualTo(SourceLineClassifier.LineType.WHATSAPP_METADATA);

        assertThat(classifier.classify("2026-08-13").type())
                .isEqualTo(SourceLineClassifier.LineType.WHATSAPP_METADATA);

        assertThat(classifier.classify("13/08/2026 Maintenance").type())
                .isEqualTo(SourceLineClassifier.LineType.WHATSAPP_METADATA);

        assertThat(classifier.classify("De 23/07/26").type())
                .isEqualTo(SourceLineClassifier.LineType.WHATSAPP_METADATA);
    }

    @Test
    void recognizesSafeWhatsappAndOcrNoiseFromRealScreenshots() {
        for (String line : new String[]{
                "15:01", "23:01", "00:40", "Aujourd'hui", "Hier", "Message",
                ":", "M", "C", "S", "KPI PRODUCTION", "Ayman Charge Alf",
                "Suivi des consommations liquides.", "PF", "Aymane", "Lokbiche"
        }) {
            assertThat(classifier.classify(line).ignored())
                    .as(line)
                    .isTrue();
        }
    }

    @Test
    void doesNotIgnoreUnknownKpiLikeContent() {
        assertThat(classifier.classify("Unexpected metric 44").type())
                .isEqualTo(SourceLineClassifier.LineType.CONTENT);

        assertThat(classifier.classify("Pression hydraulique : 12 bar").type())
                .isEqualTo(SourceLineClassifier.LineType.CONTENT);

        assertThat(classifier.classify("Recyclage 20y").type())
                .isEqualTo(SourceLineClassifier.LineType.CONTENT);

        for (String plausibleBusinessLine : new String[]{"Methionine", "Terminé 8", "Eau"}) {
            assertThat(classifier.classify(plausibleBusinessLine).ignored())
                    .as(plausibleBusinessLine)
                    .isFalse();
        }
    }

    @Test
    void doesNotIgnoreNormalKnownKpiContent() {
        assertThat(classifier.classify("Vrac : 15,8").type())
                .isEqualTo(SourceLineClassifier.LineType.CONTENT);

        assertThat(classifier.classify("Compresseur 1: 77108-77%").type())
                .isEqualTo(SourceLineClassifier.LineType.CONTENT);
    }

    @Test
    void exposesIgnoredStateConsistently() {
        assertThat(classifier.classify("KPI :").ignored())
                .isTrue();

        assertThat(classifier.classify("10:52").ignored())
                .isTrue();

        assertThat(classifier.classify("Vrac : 15").ignored())
                .isFalse();
    }
}
