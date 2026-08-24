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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class ReportDocumentGeneratorTest {

    @Test
    void createsAcceptanceArtifactsForEveryRequiredScope() throws Exception {
        LocalDate date = LocalDate.of(2026, 8, 13);
        List<ReportGenerationData.Row> allRows = List.of(
                row(date.minusDays(1), 1L, 1L, "VRAC", "Vrac", "t", new BigDecimal("40"), null, null),
                row(date, 2L, 1L, "VRAC", "Vrac", "t", new BigDecimal("42.75"), null, null),
                row(date, 2L, 2L, "HUM", "Humidité", "%", null, null, null),
                row(date, 2L, 3L, "COMP", "Compresseur 1", "h", new BigDecimal("77108"),
                        new BigDecimal("77"), "%")
        );
        Path output = Path.of("target", "verification-artifacts");
        Files.createDirectories(output);

        List<ReportGenerationData> samples = List.of(
                sample(GeneratedReportType.INDIVIDUAL, date, date, allRows.subList(1, 4)),
                sample(GeneratedReportType.DAILY, date, date, allRows.subList(1, 4)),
                sample(GeneratedReportType.WEEKLY, date.minusDays(6), date, allRows),
                sample(GeneratedReportType.MONTHLY, date.withDayOfMonth(1), date.withDayOfMonth(31), allRows)
        );

        for (ReportGenerationData sample : samples) {
            String name = sample.type().name().toLowerCase(Locale.ROOT);
            byte[] excel = new ExcelReportGenerator().generate(sample);
            byte[] pdf = new PdfReportGenerator().generate(sample);
            Files.write(output.resolve("FactoryFlow_sample_" + name + ".xlsx"), excel);
            Files.write(output.resolve("FactoryFlow_sample_" + name + ".pdf"), pdf);
            try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(excel));
                 var document = Loader.loadPDF(pdf)) {
                assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
                assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(1);
                String text = new PDFTextStripper().getText(document);
                if (sample.type() == GeneratedReportType.WEEKLY
                        || sample.type() == GeneratedReportType.MONTHLY) {
                    assertThat(text).contains("ÉVOLUTION — VRAC");
                } else {
                    assertThat(text).doesNotContain("ÉVOLUTION —");
                }
            }
        }
    }

    @Test
    void weeklyDocumentsUseOfficialBrandingAndManagerFocusedTables() throws Exception {
        LocalDate date = LocalDate.of(2026, 8, 13);

        var analytics = new ReportAnalyticsService().analyze(
                List.of(
                        new Measurement(
                                1L,
                                "VRAC",
                                "Vrac",
                                "t",
                                date.minusDays(1),
                                1L,
                                new BigDecimal("40")
                        ),
                        new Measurement(
                                1L,
                                "VRAC",
                                "Vrac",
                                "t",
                                date,
                                2L,
                                new BigDecimal("42.75")
                        ),
                        new Measurement(
                                2L,
                                "HUM",
                                "Humidité",
                                "%",
                                date,
                                2L,
                                null
                        )
                ),
                List.of()
        );

        var rows = List.of(
                row(
                        date.minusDays(1),
                        1L,
                        1L,
                        "VRAC",
                        "Vrac",
                        "t",
                        new BigDecimal("40"),
                        null,
                        null
                ),
                row(
                        date,
                        2L,
                        1L,
                        "VRAC",
                        "Vrac",
                        "t",
                        new BigDecimal("42.75"),
                        null,
                        null
                ),
                row(
                        date,
                        2L,
                        2L,
                        "HUM",
                        "Humidité",
                        "%",
                        null,
                        null,
                        null
                ),
                row(
                        date,
                        2L,
                        3L,
                        "COMP",
                        "Compresseur 1",
                        "h",
                        new BigDecimal("77108"),
                        new BigDecimal("77"),
                        "%"
                )
        );

        var data = new ReportGenerationData(
                GeneratedReportType.WEEKLY,
                new ReportPeriod(
                        date.minusDays(6),
                        date
                ),
                Instant.parse(
                        "2026-08-14T10:00:00Z"
                ),
                rows,
                analytics
        );

        byte[] excel =
                new ExcelReportGenerator()
                        .generate(data);

        try (
                XSSFWorkbook workbook =
                        new XSSFWorkbook(
                                new ByteArrayInputStream(
                                        excel
                                )
                        )
        ) {
            /*
             * Single-sheet contract: exactly one worksheet, named "Rapport".
             */
            assertThat(workbook.getNumberOfSheets())
                    .isEqualTo(1);

            assertThat(workbook.getSheetName(0))
                    .isEqualTo("Rapport");

            XSSFSheet sheet =
                    workbook.getSheet("Rapport");

            assertThat(sheet)
                    .isNotNull();

            int analysisHeaderRow =
                    findRow(
                            sheet,
                            0,
                            "ANALYSE KPI"
                    );

            assertThat(analysisHeaderRow)
                    .isGreaterThanOrEqualTo(0);

            int detailHeaderRow =
                    findRow(
                            sheet,
                            0,
                            "Date"
                    );

            assertThat(detailHeaderRow)
                    .isGreaterThan(analysisHeaderRow);

            assertThat(List.of(
                    sheet.getRow(detailHeaderRow).getCell(0).getStringCellValue(),
                    sheet.getRow(detailHeaderRow).getCell(1).getStringCellValue(),
                    sheet.getRow(detailHeaderRow).getCell(2).getStringCellValue(),
                    sheet.getRow(detailHeaderRow).getCell(3).getStringCellValue(),
                    sheet.getRow(detailHeaderRow).getCell(4).getStringCellValue()
            )).containsExactly(
                    "Date",
                    "Indicateur",
                    "Valeur",
                    "Valeur associée",
                    "Unité"
            );

            /*
             * Detail order:
             * Vrac 40
             * Vrac 42.75
             * Humidité missing
             * Compresseur composite
             */
            Row missingRow =
                    sheet.getRow(
                            detailHeaderRow + 3
                    );

            assertThat(
                    missingRow
                            .getCell(2)
                            .getCellType()
            ).isEqualTo(
                    CellType.BLANK
            );

            /*
             * Native Excel date cell, not a text representation.
             */
            assertThat(
                    missingRow
                            .getCell(0)
                            .getCellType()
            ).isEqualTo(
                    CellType.NUMERIC
            );

            assertThat(
                    org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(
                            missingRow.getCell(0)
                    )
            ).isTrue();

            Row compositeRow =
                    sheet.getRow(
                            detailHeaderRow + 4
                    );

            assertThat(
                    compositeRow
                            .getCell(2)
                            .getNumericCellValue()
            ).isEqualTo(77108d);

            assertThat(
                    compositeRow
                            .getCell(3)
                            .getNumericCellValue()
            ).isEqualTo(77d);

            /*
             * This layout pass removes charts entirely. The only drawing
             * object on the sheet is the Alf Mabrouk wordmark picture.
             */
            assertThat(
                    sheet.getDrawingPatriarch()
                            .getCharts()
            ).isEmpty();

            assertThat(workbook.getAllPictures()).hasSize(1);
            try (var officialLogo = getClass().getResourceAsStream(
                    "/reporting/alf-mabrouk-logo.png")) {
                assertThat(officialLogo).isNotNull();
                assertThat(workbook.getAllPictures().getFirst().getData())
                        .isEqualTo(officialLogo.readAllBytes());
            }

            /*
             * Per-KPI "QUALITÉ DES DONNÉES" table, distinct from the top
             * summary cards.
             */
            int qualityHeaderRow =
                    findRow(
                            sheet,
                            0,
                            "QUALITÉ DES DONNÉES"
                    );

            assertThat(qualityHeaderRow)
                    .isGreaterThan(detailHeaderRow);

            int traceabilityHeader =
                    findRow(
                            sheet,
                            0,
                            "Rapport source"
                    );

            assertThat(traceabilityHeader)
                    .isGreaterThan(qualityHeaderRow);

            int vracQualityRow =
                    findRow(
                            sheet,
                            0,
                            "Vrac",
                            qualityHeaderRow,
                            traceabilityHeader
                    );

            assertThat(vracQualityRow)
                    .isGreaterThan(qualityHeaderRow);

            assertThat(
                    sheet.getRow(vracQualityRow)
                            .getCell(1)
                            .getCellType()
            ).isEqualTo(CellType.NUMERIC);

            int auditRow =
                    findRow(
                            sheet,
                            0,
                            "Contrôle de cohérence Excel :"
                    );

            assertThat(auditRow)
                    .isEqualTo(-1);

            assertThat(
                    sheet.getRow(
                                    traceabilityHeader + 1
                            )
                            .getCell(0)
                            .getCellType()
            ).isEqualTo(
                    CellType.NUMERIC
            );
        }

        byte[] pdf =
                new PdfReportGenerator()
                        .generate(data);

        try (
                var document =
                        Loader.loadPDF(pdf)
        ) {
            String text =
                    new PDFTextStripper()
                            .getText(document);

            assertThat(text).contains(
                    "SYNTHÈSE EXÉCUTIVE",
                    "Complétude des données",
                    "77 108",
                    "77 h / %",
                    "Non renseigné",
                    "DATE",
                    "INDICATEUR",
                    "VALEUR ASSOCIÉE",
                    "UNITÉ"
            );
            assertThat(text).doesNotContain("CONFIRMED", "MANUAL", "Statut");

            assertThat(document.getPage(0).getResources().getXObjectNames())
                    .isNotEmpty();

            assertThat(
                    document
                            .getDocumentInformation()
                            .getCreator()
            ).isEqualTo("FactoryFlow");
        }
    }

    private int findRow(
            XSSFSheet sheet,
            int column,
            String expected
    ) {
        return findRow(sheet, column, expected, 0, sheet.getLastRowNum());
    }

    /**
     * Searches for {@code expected} in {@code column}, restricted to rows
     * in the inclusive range [fromRow, toRow]. Used to confirm a row (e.g.
     * a KPI name) appears within a specific section rather than anywhere
     * on the sheet.
     */
    private int findRow(
            XSSFSheet sheet,
            int column,
            String expected,
            int fromRow,
            int toRow
    ) {
        for (
                int rowIndex = fromRow;
                rowIndex <= toRow;
                rowIndex++
        ) {
            Row row =
                    sheet.getRow(rowIndex);

            if (row == null) {
                continue;
            }

            Cell cell =
                    row.getCell(column);

            if (
                    cell != null
                            && cell.getCellType()
                            == CellType.STRING
                            && expected.equals(
                            cell.getStringCellValue()
                    )
            ) {
                return rowIndex;
            }
        }

        return -1;
    }

    private ReportGenerationData.Row row(
            LocalDate date,
            Long reportId,
            Long kpiId,
            String code,
            String name,
            String unit,
            BigDecimal value,
            BigDecimal secondary,
            String secondaryUnit
    ) {
        return new ReportGenerationData.Row(
                date,
                reportId,
                AcquisitionSource.MANUAL,
                kpiId,
                code,
                name,
                unit,
                value,
                secondary,
                secondaryUnit,
                "Ingénieur maintenance",
                Instant.parse(
                        "2026-08-13T16:00:00Z"
                )
        );
    }

    private ReportGenerationData sample(
            GeneratedReportType type,
            LocalDate start,
            LocalDate end,
            List<ReportGenerationData.Row> rows
    ) {
        var measurements = rows.stream().map(value -> new Measurement(
                value.kpiDefinitionId(),
                value.kpiCode(),
                value.kpiName(),
                value.unit(),
                value.effectiveDate(),
                value.sourceReportId(),
                value.confirmedValue()
        )).toList();
        return new ReportGenerationData(
                type,
                new ReportPeriod(start, end),
                Instant.parse("2026-08-14T10:00:00Z"),
                rows,
                new ReportAnalyticsService().analyze(measurements, List.of())
        );
    }
}
