package com.factoryflow.parser;

import static org.assertj.core.api.Assertions.assertThat;

import com.factoryflow.parser.api.AnalyzeReportRequest;
import com.factoryflow.parser.api.AnalyzeReportResponse;
import com.factoryflow.parser.api.ParsedEntry;
import com.factoryflow.parser.application.ReportAnalysisService;
import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.kpi.persistence.KpiDefinitionRepository;
import com.factoryflow.report.domain.AcquisitionSource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ParserRegressionTest {

    @Autowired ReportAnalysisService analysis;
    @Autowired KpiDefinitionRepository definitions;

    @ParameterizedTest(name = "{0}")
    @MethodSource("formatVariants")
    void parsesRealisticSeparatorAndNumberVariants(String scenario, String input, String code, String expected) {
        AnalyzeReportResponse result = analyze(input);

        assertThat(result.entries()).hasSize(1);
        assertThat(result.entries().getFirst().kpiCode()).isEqualTo(code);
        assertThat(result.entries().getFirst().extractedValue()).isEqualByComparingTo(expected);
        assertThat(result.rawText()).isEqualTo(input);
    }

    static Stream<Arguments> formatVariants() {
        return Stream.of(
                Arguments.of("colon", "Vrac: 15,8", "VRAC", "15.8"),
                Arguments.of("equals", "Vrac = 12.5", "VRAC", "12.5"),
                Arguments.of("arrow", "Choline -> 295456", "CHOLINE", "295456"),
                Arguments.of("whitespace", "Sac   18,2", "SAC", "18.2"),
                Arguments.of("attached unit", "Total: 33,4t", "TOTAL", "33.4"),
                Arguments.of("uppercase and Windows line ending", "VRAC : 16\r\n", "VRAC", "16"),
                Arguments.of("grouped integer", "Choline: 1 250", "CHOLINE", "1250"),
                Arguments.of("exact alias", "Varc : 14", "VRAC", "14"),
                Arguments.of("minor fuzzy typo", "Cholin : 7", "CHOLINE", "7")
        );
    }

    @Test
    void preservesMissingUnknownDuplicateAndPartialContent() {
        String input = """
                [10:52] Maintenance group
                Vrac : ----
                Sac : 18
                Sac = 19
                Unexpected metric 44
                """;

        AnalyzeReportResponse result = analyze(input);

        assertThat(result.entries()).hasSize(3);
        assertThat(find(result, "VRAC").extractedValue()).isNull();
        assertThat(find(result, "VRAC").warnings()).extracting("code").contains("MISSING_VALUE");
        assertThat(result.entries().stream().filter(entry -> "SAC".equals(entry.kpiCode())))
                .allSatisfy(entry -> assertThat(entry.warnings()).extracting("code").contains("DUPLICATE_KPI"));
        assertThat(result.unrecognizedLines()).extracting("sourceLine")
                .contains("[10:52] Maintenance group", "Unexpected metric 44");
        assertThat(result.unrecognizedCount()).isEqualTo(2);
    }

    @Test
    void handlesAmbiguousThousandsAndMultipleValuesWithoutCombiningThem() {
        AnalyzeReportResponse result = analyze("""
                Fuel : 30.197
                Compresseur 1: 77108-77%
                """);

        assertThat(find(result, "FUEL").extractedValue()).isEqualByComparingTo("30197");
        assertThat(find(result, "FUEL").warnings()).extracting("code").contains("AMBIGUOUS_NUMBER");
        assertThat(result.entries()).hasSize(3);
        assertThat(result.entries().get(1).extractedValue()).isEqualByComparingTo("77108");
        assertThat(result.entries().get(2).extractedValue()).isEqualByComparingTo("77");
        assertThat(result.entries().get(2).kpiDefinitionId()).isNull();
        assertThat(result.entries().get(2).warnings()).extracting("code")
                .contains("ADDITIONAL_VALUE_REQUIRES_ASSIGNMENT");
    }

    @Test
    void doesNotInventZeroForMissingOrAbsentKpis() {
        AnalyzeReportResponse result = analyze("Vrac : ---");

        assertThat(result.entries()).singleElement().satisfies(entry -> {
            assertThat(entry.extractedValue()).isNull();
            assertThat(entry.kpiCode()).isEqualTo("VRAC");
        });
        assertThat(result.entries()).noneMatch(entry -> BigDecimal.ZERO.equals(entry.extractedValue()));
    }

    @Test
    void treatsMultipleWhatsAppBubblesAsOneReviewFlowAndKeepsTheirNoise() {
        String input = """
                10:52
                Vrac : 15,8
                10:53
                Choline = 295456
                """;

        AnalyzeReportResponse result = analyze(input);

        assertThat(result.entries()).extracting(ParsedEntry::kpiCode).containsExactly("VRAC", "CHOLINE");
        assertThat(result.unrecognizedLines()).extracting("sourceLine").containsExactly("10:52", "10:53");
        assertThat(result.rawText()).isEqualTo(input);
    }

    @Test
    void reportsInvalidNumbersAndPlausibilityWarningsWithoutCorrectingThem() {
        definitions.saveAndFlush(KpiDefinition.create(
                "TEST_PRESSURE", "Test Pressure", "Test", "bar",
                BigDecimal.ZERO, new BigDecimal("100"), true, List.of("Pressure Test")
        ));

        AnalyzeReportResponse result = analyze("""
                Test Pressure : 150bar
                Vrac : unavailable
                Total :
                """);

        assertThat(find(result, "TEST_PRESSURE").extractedValue()).isEqualByComparingTo("150");
        assertThat(find(result, "TEST_PRESSURE").warnings()).extracting("code").contains("OUTSIDE_PLAUSIBLE_RANGE");
        assertThat(find(result, "VRAC").extractedValue()).isNull();
        assertThat(find(result, "VRAC").warnings()).extracting("code").contains("INVALID_NUMBER");
        assertThat(find(result, "TOTAL").extractedValue()).isNull();
        assertThat(find(result, "TOTAL").warnings()).extracting("code").contains("INVALID_NUMBER");
    }

    @Test
    void recognizesKpisIndependentlyOfTheirOrder() {
        AnalyzeReportResponse result = analyze("""
                Total: 30
                Sac: 10
                Vrac: 20
                """);

        assertThat(result.entries()).extracting(ParsedEntry::kpiCode).containsExactly("TOTAL", "SAC", "VRAC");
    }

    private AnalyzeReportResponse analyze(String input) {
        return analysis.analyze(new AnalyzeReportRequest(input, AcquisitionSource.PASTE));
    }

    private ParsedEntry find(AnalyzeReportResponse response, String code) {
        return response.entries().stream().filter(entry -> code.equals(entry.kpiCode())).findFirst().orElseThrow();
    }
}
