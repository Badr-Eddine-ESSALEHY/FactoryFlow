package com.factoryflow.generatedreport;

import static org.assertj.core.api.Assertions.assertThat;

import com.factoryflow.analytics.application.ReportAnalyticsService;
import com.factoryflow.analytics.application.ReportAnalyticsService.Measurement;
import com.factoryflow.generatedreport.application.ExcelReportGenerator;
import com.factoryflow.generatedreport.application.PdfReportGenerator;
import com.factoryflow.generatedreport.application.ReportGenerationData;
import com.factoryflow.generatedreport.domain.GeneratedReportType;
import com.factoryflow.generatedreport.domain.ReportPeriod;
import com.factoryflow.report.domain.AcquisitionSource;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class ReportDocumentGeneratorTest {
    @Test
    void weeklyDocumentsReopenWithAnalyticsChartsCompositeValuesAndBlankMissingCells() throws Exception {
        LocalDate date = LocalDate.of(2026, 8, 13);
        var analytics = new ReportAnalyticsService().analyze(List.of(
                new Measurement(1L, "VRAC", "Vrac", "t", date.minusDays(1), 1L, new BigDecimal("40")),
                new Measurement(1L, "VRAC", "Vrac", "t", date, 2L, new BigDecimal("42.75")),
                new Measurement(2L, "HUM", "Humidité", "%", date, 2L, null)
        ), List.of());
        var rows = List.of(
                row(date.minusDays(1), 1L, 1L, "VRAC", "Vrac", "t", new BigDecimal("40"), null, null),
                row(date, 2L, 1L, "VRAC", "Vrac", "t", new BigDecimal("42.75"), null, null),
                row(date, 2L, 2L, "HUM", "Humidité", "%", null, null, null),
                row(date, 2L, 3L, "COMP", "Compresseur 1", "h", new BigDecimal("77108"), new BigDecimal("77"), "%")
        );
        var data = new ReportGenerationData(GeneratedReportType.WEEKLY, new ReportPeriod(date.minusDays(6), date),
                Instant.parse("2026-08-14T10:00:00Z"), rows, analytics);

        byte[] excel = new ExcelReportGenerator().generate(data);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(5);
            assertThat(workbook.getSheet("Données").getRow(8).getCell(2).getCellType()).isEqualTo(CellType.BLANK);
            assertThat(workbook.getSheet("Analyse KPI").getDrawingPatriarch().getCharts()).hasSize(1);
            assertThat(workbook.getSheet("Traçabilité").getRow(1).getCell(1).getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(workbook.getSheet("Données").getSheetConditionalFormatting().getNumConditionalFormattings()).isEqualTo(1);
            assertThat(workbook.getSheet("Qualité des données").getRow(7).getCell(2).getCellType()).isEqualTo(CellType.FORMULA);
        }

        byte[] pdf = new PdfReportGenerator().generate(data);
        try (var document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);
            assertThat(text).contains("SYNTHÈSE EXÉCUTIVE", "Complétude des données", "77 108", "77%", "Non renseigné");
            assertThat(document.getDocumentInformation().getCreator()).isEqualTo("FactoryFlow");
        }
    }

    private ReportGenerationData.Row row(LocalDate date, Long reportId, Long kpiId, String code, String name,
                                         String unit, BigDecimal value, BigDecimal secondary, String secondaryUnit) {
        return new ReportGenerationData.Row(date, reportId, AcquisitionSource.MANUAL, kpiId, code, name, unit,
                value, secondary, secondaryUnit, "Ingénieur maintenance", Instant.parse("2026-08-13T16:00:00Z"));
    }
}
