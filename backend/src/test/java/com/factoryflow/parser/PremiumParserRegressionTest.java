package com.factoryflow.parser;

import static org.assertj.core.api.Assertions.assertThat;

import com.factoryflow.parser.api.AnalyzeReportRequest;
import com.factoryflow.parser.api.AnalyzeReportResponse;
import com.factoryflow.parser.api.ParsedEntry;
import com.factoryflow.parser.application.ReportAnalysisService;
import com.factoryflow.report.domain.AcquisitionSource;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PremiumParserRegressionTest {

    @Autowired
    ReportAnalysisService analysis;

    @Test
    void parsesRecurringWhatsappFormatAsExceptionReviewData() {
        AnalyzeReportResponse result = analyze("""
                KPI :
                Compresseur 1: 77108-77%
                Compresseur 2: 68232-26%
                Temps sécheur : 5°C
                Compteur eau : 50380
                Durée l'eau adous : 4
                Compteur Eurotech: 7878
                Compteur Cicalim : 3046
                Heures Fct P1 : 62104
                Q. Produit P1 : 4402,4
                Heures Fct P2 : 1831
                Q. Produit P2 : 103249.8
                Heures de Fct Broyeur 1: 16650
                Heures de Fct Broyeur 2: 16398
                Niveau citerne Huile: 36945
                Niveau citerne Mélasse: ----
                Quantité graisse P1:----
                Quantité graisse P2:----
                """);

        assertThat(result.ignoredLines()).singleElement().satisfies(line -> {
            assertThat(line.sourceLine()).isEqualTo("KPI :");
            assertThat(line.classification()).isEqualTo("HEADER");
        });
        assertThat(result.entries()).extracting(ParsedEntry::kpiCode)
                .contains("COMPRESSEUR_1", "COMPRESSEUR_2", "WATER_METER", "MOLASSES_TANK_LEVEL");
        assertThat(find(result, "COMPRESSEUR_1").extractedValue()).isEqualByComparingTo("77108");
        assertThat(find(result, "COMPRESSEUR_2").extractedValue()).isEqualByComparingTo("68232");
        assertThat(find(result, "MOLASSES_TANK_LEVEL").reviewState()).isEqualTo("MISSING");
        assertThat(find(result, "MOLASSES_TANK_LEVEL").extractedValue()).isNull();
        assertThat(result.missingCount()).isEqualTo(3);
        assertThat(result.unrecognizedLines()).isEmpty();
        assertThat(result.entries().stream().filter(entry -> entry.warnings().stream()
                        .anyMatch(warning -> warning.code().equals("DUPLICATE_KPI"))))
                .isEmpty();
    }

    @Test
    void recognizesAllMissingSentinelsWithoutCreatingZeros() {
        AnalyzeReportResponse result = analyze("""
                Vrac: -
                Sac: --
                Total: N/A
                Fuel: vide
                Compresseur 1: manquant
                """);

        assertThat(result.entries()).hasSize(5);
        assertThat(result.entries()).allSatisfy(entry -> {
            assertThat(entry.reviewState()).isEqualTo("MISSING");
            assertThat(entry.extractedValue()).isNull();
        });
        assertThat(result.entries()).noneMatch(entry -> BigDecimal.ZERO.equals(entry.extractedValue()));
    }

    @Test
    void unknownLabelsReceiveDeterministicSuggestionsWithoutSilentAuthority() {
        AnalyzeReportResponse result = analyze("Compteur Erotech: 7878");

        assertThat(result.entries()).singleElement().satisfies(entry -> {
            assertThat(entry.reviewState()).isEqualTo("ATTENTION");
            assertThat(entry.matchMethod()).isEqualTo("FUZZY_SUGGESTION");
            assertThat(entry.suggestions()).isNotEmpty();
        });
        assertThat(result.attentionCount()).isEqualTo(1);
    }

    private AnalyzeReportResponse analyze(String input) {
        return analysis.analyze(new AnalyzeReportRequest(input, AcquisitionSource.PASTE));
    }

    private ParsedEntry find(AnalyzeReportResponse response, String code) {
        return response.entries().stream().filter(entry -> code.equals(entry.kpiCode())).findFirst().orElseThrow();
    }
}
