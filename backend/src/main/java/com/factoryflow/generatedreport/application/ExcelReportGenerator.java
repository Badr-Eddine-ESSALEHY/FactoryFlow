package com.factoryflow.generatedreport.application;

import com.factoryflow.analytics.domain.AnalyticsSnapshot;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class ExcelReportGenerator {

    /*
     * One-sheet reporting contract — final layout pass.
     *
     * Everything a manager needs is gathered in one scrollable "Rapport"
     * worksheet, bounded to columns A:I so it reads at normal zoom with no
     * horizontal scrolling:
     * - header (wordmark + title, side by side)
     * - one global summary block
     * - KPI analysis
     * - confirmed data detail
     * - per-KPI data-quality table
     * - traceability
     * - footer
     *
     * This pass deliberately contains NO charts. The previous temporal
     * chart (and its hidden W:X helper columns) has been removed entirely;
     * with the current volume of confirmed data a chart added more empty
     * space than insight. The Alf Mabrouk wordmark remains the only
     * drawing object on the sheet and is rendered at runtime, like the PDF
     * — no PNG resource is required.
     */

    private static final String SHEET_NAME = "Rapport";

    /** Visible report is bounded to columns A(0) .. I(8). */
    private static final int LAST_VISIBLE_COLUMN = 8;

    private static final String[] ANALYSIS_HEADERS = {
            "Indicateur",
            "Unité",
            "Dernière",
            "Moyenne",
            "Minimum",
            "Maximum",
            "Δ période",
            "Tendance",
            "Complétude"
    };

    private static final String[] DETAIL_HEADERS = {
            "Date effective",
            "Indicateur",
            "Valeur confirmée",
            "Valeur associée",
            "Unité",
            "Statut",
            "Rapport source",
            "Confirmé le"
    };

    private static final String[] QUALITY_HEADERS = {
            "Indicateur",
            "Mesures valides",
            "Non renseignées",
            "Rapports",
            "Complétude"
    };

    private static final String[] TRACE_HEADERS = {
            "Rapport source",
            "Date effective",
            "Source",
            "Soumis par",
            "Confirmé le"
    };

    /*
     * Light, comfortable workbook palette. Corporate magenta/green are
     * used only inside the wordmark, as the user explicitly rejected dark
     * navy / dark blocks elsewhere in the workbook.
     */
    private static final byte[] TEXT = rgb(0x40, 0x48, 0x4D);
    private static final byte[] MUTED = rgb(0x74, 0x7C, 0x80);

    private static final byte[] SAGE = rgb(0x8E, 0xAD, 0x9D);
    private static final byte[] SECTION_BG = rgb(0xE7, 0xF0, 0xEB);
    private static final byte[] TABLE_HEADER_BG = rgb(0xD4, 0xE5, 0xDC);
    private static final byte[] ALT_ROW = rgb(0xF7, 0xF8, 0xF7);
    private static final byte[] BORDER = rgb(0xD9, 0xDE, 0xDC);
    private static final byte[] WARM_WHITE = rgb(0xFC, 0xFB, 0xF8);

    private static final byte[] CARD_BLUE = rgb(0xEA, 0xF3, 0xF8);
    private static final byte[] CARD_MINT = rgb(0xEA, 0xF4, 0xED);
    private static final byte[] CARD_PEACH = rgb(0xFA, 0xEE, 0xE4);
    private static final byte[] CARD_LAVENDER = rgb(0xF1, 0xEC, 0xF8);

    private static final byte[] CONFIRMED_GREEN = rgb(0x3E, 0x7A, 0x5E);

    private static final byte[] LOGO_MAGENTA = rgb(194, 47, 138);
    private static final byte[] LOGO_GREEN = rgb(91, 154, 47);

    public byte[] generate(ReportGenerationData data) {
        try (
                XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output = new ByteArrayOutputStream()
        ) {
            configureWorkbookProperties(workbook, data);

            Styles styles = createStyles(workbook);
            XSSFSheet sheet = workbook.createSheet(SHEET_NAME);

            configureSheet(sheet);

            byte[] wordmark = createCorporateWordmark();
            addWordmark(workbook, sheet, wordmark);

            int analysisStartRow = renderHeaderAndSummary(sheet, data, styles);

            int analysisEnd = renderAnalysis(
                    sheet,
                    data,
                    styles,
                    analysisStartRow
            );

            int detailStart = analysisEnd + 2;
            int detailEnd = renderDetail(
                    sheet,
                    data,
                    styles,
                    detailStart
            );

            int qualityStart = detailEnd + 2;
            int qualityEnd = renderQuality(
                    sheet,
                    data,
                    styles,
                    qualityStart
            );

            int traceStart = qualityEnd + 2;
            int reportEnd = renderTraceability(
                    sheet,
                    data,
                    styles,
                    traceStart
            );

            sheet.createFreezePane(0, 5);

            workbook.setPrintArea(
                    0,
                    0,
                    LAST_VISIBLE_COLUMN,
                    0,
                    reportEnd
            );

            workbook.setActiveSheet(0);
            workbook.setSelectedTab(0);

            workbook.write(output);
            return output.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Excel workbook generation failed",
                    exception
            );
        }
    }

    private void configureWorkbookProperties(
            XSSFWorkbook workbook,
            ReportGenerationData data
    ) {
        var properties = workbook.getProperties().getCoreProperties();

        properties.setCreator(ReportDocumentText.PRODUCT);
        properties.setTitle(ReportDocumentText.title(data.type()));
        properties.setSubjectProperty(
                "Indicateurs de maintenance confirmés - "
                        + ReportDocumentText.period(data)
        );
        properties.setDescription(
                "Rapport FactoryFlow généré pour Alf Mabrouk."
        );
    }

    private void configureSheet(XSSFSheet sheet) {
        sheet.setDisplayGridlines(false);
        sheet.setPrintGridlines(false);
        sheet.setZoom(100);

        sheet.getPrintSetup().setLandscape(true);
        sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
        sheet.getPrintSetup().setFitWidth((short) 1);
        sheet.getPrintSetup().setFitHeight((short) 0);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);

        sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.LeftMargin, .35);
        sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.RightMargin, .35);
        sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.TopMargin, .4);
        sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.BottomMargin, .4);

        /*
         * The visible report is bounded to A:I. Column E is intentionally
         * wider than the reference 11-unit target because it also carries
         * "Confirmé le" timestamps (dd/mm/yyyy hh:mm) in the Traçabilité
         * section — at 11 units that column rendered as "##########".
         * Every other width matches the requested target closely.
         */
        int[] widths = {
                18, 22, 14, 14, 17, 11, 13, 18, 13
        };

        setColumnWidths(sheet, widths);
    }

    private int renderHeaderAndSummary(
            XSSFSheet sheet,
            ReportGenerationData data,
            Styles styles
    ) {
        /*
         * Rows 0-3: large runtime wordmark at A:D, title/period/status
         * immediately beside it at E:I. White / warm-white canvas, with
         * only a thin soft-sage divider under the header — no dark block.
         */
        getOrCreateRow(sheet, 0).setHeightInPoints(42);
        getOrCreateRow(sheet, 1).setHeightInPoints(28);
        getOrCreateRow(sheet, 2).setHeightInPoints(24);
        getOrCreateRow(sheet, 3).setHeightInPoints(6);

        mergeText(
                sheet,
                0,
                0,
                4,
                LAST_VISIBLE_COLUMN,
                ReportDocumentText.title(data.type()),
                styles.title()
        );

        mergeText(
                sheet,
                1,
                1,
                4,
                LAST_VISIBLE_COLUMN,
                ReportDocumentText.period(data),
                styles.subtitle()
        );

        mergeText(
                sheet,
                2,
                2,
                4,
                LAST_VISIBLE_COLUMN,
                ReportDocumentText.CONFIRMED
                        + " · Généré le "
                        + ReportDocumentText.instant(data.generatedAt()),
                styles.headerMeta()
        );

        mergeText(
                sheet,
                3,
                3,
                0,
                LAST_VISIBLE_COLUMN,
                "",
                styles.accentLine()
        );

        String completeness = data.analytics().completenessRate() == null
                ? "—"
                : data.analytics().completenessRate()
                .stripTrailingZeros()
                .toPlainString() + " %";

        /*
         * The ONLY global summary block in the workbook. Rows 5-7,
         * four compact pastel cards spanning A:H — column I is left
         * unused in this row, as specified.
         */
        summaryCard(
                sheet,
                5,
                0,
                1,
                "Rapports confirmés",
                Long.toString(data.analytics().reportCount()),
                styles.cardBlue()
        );

        summaryCard(
                sheet,
                5,
                2,
                3,
                "Mesures confirmées",
                Long.toString(data.analytics().measurementCount()),
                styles.cardMint()
        );

        summaryCard(
                sheet,
                5,
                4,
                5,
                "Non renseignées",
                Long.toString(data.analytics().missingValueCount()),
                styles.cardPeach()
        );

        summaryCard(
                sheet,
                5,
                6,
                7,
                "Complétude",
                completeness,
                styles.cardLavender()
        );

        mergeText(
                sheet,
                8,
                8,
                0,
                LAST_VISIBLE_COLUMN,
                "Vue consolidée des données confirmées. Les valeurs absentes "
                        + "restent manquantes et ne sont jamais transformées en zéro.",
                styles.intro()
        );

        return 10;
    }

    private int renderAnalysis(
            XSSFSheet sheet,
            ReportGenerationData data,
            Styles styles,
            int startRow
    ) {
        mergeText(
                sheet,
                startRow,
                startRow,
                0,
                LAST_VISIBLE_COLUMN,
                "ANALYSE KPI",
                styles.section()
        );

        Row header = getOrCreateRow(sheet, startRow + 1);
        header.setHeightInPoints(27);

        for (int column = 0; column < ANALYSIS_HEADERS.length; column++) {
            text(
                    header,
                    column,
                    ANALYSIS_HEADERS[column],
                    styles.tableHeader()
            );
        }

        int rowIndex = startRow + 2;

        for (AnalyticsSnapshot.KpiAnalytics kpi : data.analytics().kpis()) {
            Row row = getOrCreateRow(sheet, rowIndex);
            row.setHeightInPoints(23);

            boolean alternate = rowIndex % 2 == 0;
            CellStyle body = alternate ? styles.alternate() : styles.body();
            CellStyle numeric = alternate ? styles.alternateDecimal() : styles.decimal();
            CellStyle bold = alternate ? styles.alternateBold() : styles.bodyBold();

            text(row, 0, kpi.displayName(), bold);
            text(row, 1, ReportDocumentText.unit(kpi.unit()), body);

            nullableNumeric(row, 2, kpi.latest(), numeric, styles.missing());
            nullableNumeric(row, 3, kpi.mean(), numeric, styles.missing());
            nullableNumeric(row, 4, kpi.minimum(), numeric, styles.missing());
            nullableNumeric(row, 5, kpi.maximum(), numeric, styles.missing());
            nullableNumeric(row, 6, kpi.periodDelta(), numeric, styles.missing());

            text(
                    row,
                    7,
                    trend(kpi.trend().name()),
                    body
            );

            if (kpi.completenessRate() == null) {
                blank(row, 8, styles.missing());
            } else {
                numeric(
                        row,
                        8,
                        kpi.completenessRate(),
                        styles.completeness()
                );
            }

            rowIndex++;
        }

        if (data.analytics().kpis().isEmpty()) {
            mergeText(
                    sheet,
                    rowIndex,
                    rowIndex,
                    0,
                    LAST_VISIBLE_COLUMN,
                    "Données insuffisantes pour produire une analyse KPI.",
                    styles.note()
            );
            rowIndex++;
        } else if (!hasTrendableKpi(data)) {
            mergeText(
                    sheet,
                    rowIndex,
                    rowIndex,
                    0,
                    LAST_VISIBLE_COLUMN,
                    "Évolution temporelle : données insuffisantes pour établir "
                            + "une tendance fiable (minimum 2 mesures confirmées "
                            + "pour un même KPI).",
                    styles.secondaryNote()
            );
            rowIndex++;
        }

        return rowIndex - 1;
    }

    private int renderDetail(
            XSSFSheet sheet,
            ReportGenerationData data,
            Styles styles,
            int startRow
    ) {
        mergeText(
                sheet,
                startRow,
                startRow,
                0,
                LAST_VISIBLE_COLUMN,
                "DÉTAIL DES INDICATEURS CONFIRMÉS",
                styles.section()
        );

        mergeText(
                sheet,
                startRow + 1,
                startRow + 1,
                0,
                LAST_VISIBLE_COLUMN,
                "Les cellules vides représentent des valeurs non renseignées.",
                styles.secondaryNote()
        );

        Row header = getOrCreateRow(sheet, startRow + 2);
        header.setHeightInPoints(28);

        for (int column = 0; column < DETAIL_HEADERS.length; column++) {
            text(
                    header,
                    column,
                    DETAIL_HEADERS[column],
                    styles.tableHeader()
            );
        }

        int rowIndex = startRow + 3;

        for (ReportGenerationData.Row value : data.rows()) {
            Row row = getOrCreateRow(sheet, rowIndex);
            row.setHeightInPoints(24);

            boolean alternate = rowIndex % 2 == 0;
            CellStyle body = alternate ? styles.alternate() : styles.body();
            CellStyle bold = alternate ? styles.alternateBold() : styles.bodyBold();
            CellStyle decimal = alternate ? styles.alternateDecimal() : styles.decimal();

            Cell dateCell = row.createCell(0);
            dateCell.setCellValue(value.effectiveDate());
            dateCell.setCellStyle(
                    alternate ? styles.alternateDate() : styles.date()
            );

            text(row, 1, value.kpiName(), bold);

            if (value.confirmedValue() == null) {
                blank(row, 2, styles.missing());
            } else {
                numeric(row, 2, value.confirmedValue(), decimal);
            }

            if (value.secondaryConfirmedValue() == null) {
                blank(row, 3, styles.notApplicable());
            } else if ("%".equals(value.secondaryUnit())) {
                numeric(
                        row,
                        3,
                        value.secondaryConfirmedValue(),
                        styles.secondaryPercent()
                );
            } else {
                numeric(
                        row,
                        3,
                        value.secondaryConfirmedValue(),
                        decimal
                );
            }

            text(
                    row,
                    4,
                    ReportDocumentText.unit(value.unit()),
                    body
            );

            text(
                    row,
                    5,
                    ReportDocumentText.CONFIRMED,
                    styles.confirmed()
            );

            text(
                    row,
                    6,
                    "N°" + value.sourceReportId(),
                    body
            );

            Cell confirmedAt = row.createCell(7);

            if (value.confirmedAt() == null) {
                confirmedAt.setBlank();
            } else {
                confirmedAt.setCellValue(
                        LocalDateTime.ofInstant(
                                value.confirmedAt(),
                                ReportDocumentText.BUSINESS_ZONE
                        )
                );
            }

            confirmedAt.setCellStyle(styles.dateTime());

            rowIndex++;
        }

        if (data.rows().isEmpty()) {
            mergeText(
                    sheet,
                    rowIndex,
                    rowIndex,
                    0,
                    LAST_VISIBLE_COLUMN,
                    "Aucune donnée de maintenance confirmée pour cette période.",
                    styles.note()
            );

            rowIndex++;
        }

        sheet.setAutoFilter(
                new CellRangeAddress(
                        startRow + 2,
                        Math.max(startRow + 2, rowIndex - 1),
                        0,
                        DETAIL_HEADERS.length - 1
                )
        );

        return rowIndex - 1;
    }

    private int renderQuality(
            XSSFSheet sheet,
            ReportGenerationData data,
            Styles styles,
            int startRow
    ) {
        /*
         * Per-KPI data-quality table — not a repeat of the top summary
         * cards. Valid/missing/report counts are derived directly from
         * the confirmed detail rows for each KPI (matched by display
         * name), and completeness reuses the same figure already surfaced
         * in the Analyse KPI table, so nothing here is invented.
         */
        mergeText(
                sheet,
                startRow,
                startRow,
                0,
                LAST_VISIBLE_COLUMN,
                "QUALITÉ DES DONNÉES",
                styles.section()
        );

        Row header = getOrCreateRow(sheet, startRow + 1);
        header.setHeightInPoints(27);

        for (int column = 0; column < QUALITY_HEADERS.length; column++) {
            text(
                    header,
                    column,
                    QUALITY_HEADERS[column],
                    styles.tableHeader()
            );
        }

        int rowIndex = startRow + 2;

        for (AnalyticsSnapshot.KpiAnalytics kpi : data.analytics().kpis()) {
            long validCount = 0;
            long missingCount = 0;
            Set<Long> reportIds = new HashSet<>();

            for (ReportGenerationData.Row value : data.rows()) {
                if (!kpi.displayName().equals(value.kpiName())) {
                    continue;
                }

                reportIds.add(value.sourceReportId());

                if (value.confirmedValue() == null) {
                    missingCount++;
                } else {
                    validCount++;
                }
            }

            Row row = getOrCreateRow(sheet, rowIndex);
            row.setHeightInPoints(22);

            boolean alternate = rowIndex % 2 == 0;
            CellStyle body = alternate ? styles.alternate() : styles.body();
            CellStyle bold = alternate ? styles.alternateBold() : styles.bodyBold();
            CellStyle numericStyle = alternate ? styles.alternateDecimal() : styles.decimal();

            text(row, 0, kpi.displayName(), bold);
            numeric(row, 1, validCount, numericStyle);
            numeric(row, 2, missingCount, numericStyle);
            numeric(row, 3, reportIds.size(), numericStyle);

            if (kpi.completenessRate() == null) {
                blank(row, 4, styles.missing());
            } else {
                numeric(row, 4, kpi.completenessRate(), styles.completeness());
            }

            rowIndex++;
        }

        if (data.analytics().kpis().isEmpty()) {
            mergeText(
                    sheet,
                    rowIndex,
                    rowIndex,
                    0,
                    LAST_VISIBLE_COLUMN,
                    "Aucun indicateur à évaluer pour cette période.",
                    styles.note()
            );
            rowIndex++;
        }

        /*
         * One subtle technical audit line — a real Excel formula, not a
         * giant section — counting the true numeric cells in the "Valeur
         * confirmée" column of the detail table above.
         */
        rowIndex++;

        Row auditRow = getOrCreateRow(sheet, rowIndex);
        auditRow.setHeightInPoints(20);

        mergeText(
                sheet,
                rowIndex,
                rowIndex,
                0,
                3,
                "Contrôle de cohérence Excel :",
                styles.metadataLabel()
        );

        Cell auditCell = auditRow.getCell(4);

        if (auditCell == null) {
            auditCell = auditRow.createCell(4);
        }

        int detailHeaderRow = findDetailHeaderRow(sheet);
        int firstDataExcelRow = detailHeaderRow + 2;
        int lastDataExcelRow = Math.max(
                firstDataExcelRow,
                firstDataExcelRow + data.rows().size() - 1
        );

        auditCell.setCellFormula(
                "COUNT(C"
                        + firstDataExcelRow
                        + ":C"
                        + lastDataExcelRow
                        + ")"
        );

        auditCell.setCellStyle(styles.auditFormula());

        mergeText(
                sheet,
                rowIndex,
                rowIndex,
                5,
                LAST_VISIBLE_COLUMN,
                "cellules numériques confirmées.",
                styles.secondaryNote()
        );

        return rowIndex;
    }

    private int renderTraceability(
            XSSFSheet sheet,
            ReportGenerationData data,
            Styles styles,
            int startRow
    ) {
        mergeText(
                sheet,
                startRow,
                startRow,
                0,
                LAST_VISIBLE_COLUMN,
                "TRAÇABILITÉ",
                styles.section()
        );

        Row header = getOrCreateRow(sheet, startRow + 2);
        header.setHeightInPoints(27);

        for (int column = 0; column < TRACE_HEADERS.length; column++) {
            text(
                    header,
                    column,
                    TRACE_HEADERS[column],
                    styles.tableHeader()
            );
        }

        int rowIndex = startRow + 3;

        Set<Long> seen = new LinkedHashSet<>();

        for (ReportGenerationData.Row value : data.rows()) {
            if (!seen.add(value.sourceReportId())) {
                continue;
            }

            Row row = getOrCreateRow(sheet, rowIndex);
            row.setHeightInPoints(28);

            boolean alternate = rowIndex % 2 == 0;
            CellStyle body = alternate ? styles.alternate() : styles.body();

            numeric(
                    row,
                    0,
                    value.sourceReportId(),
                    body
            );

            Cell dateCell = row.createCell(1);
            dateCell.setCellValue(value.effectiveDate());
            dateCell.setCellStyle(
                    alternate ? styles.alternateDate() : styles.date()
            );

            text(
                    row,
                    2,
                    source(value.source().name()),
                    body
            );

            text(
                    row,
                    3,
                    value.submittedBy(),
                    body
            );

            Cell confirmedAt = row.createCell(4);

            if (value.confirmedAt() == null) {
                confirmedAt.setBlank();
            } else {
                confirmedAt.setCellValue(
                        LocalDateTime.ofInstant(
                                value.confirmedAt(),
                                ReportDocumentText.BUSINESS_ZONE
                        )
                );
            }

            confirmedAt.setCellStyle(styles.dateTime());
            rowIndex++;
        }

        if (seen.isEmpty()) {
            mergeText(
                    sheet,
                    rowIndex,
                    rowIndex,
                    0,
                    LAST_VISIBLE_COLUMN,
                    "Aucun rapport source confirmé pour cette période.",
                    styles.note()
            );
            rowIndex++;
        }

        mergeText(
                sheet,
                rowIndex + 1,
                rowIndex + 1,
                0,
                LAST_VISIBLE_COLUMN,
                "Alf Mabrouk · "
                        + ReportDocumentText.PRODUCT
                        + " · Généré automatiquement le "
                        + ReportDocumentText.instant(data.generatedAt()),
                styles.footer()
        );

        return rowIndex + 1;
    }

    private boolean hasTrendableKpi(ReportGenerationData data) {
        for (AnalyticsSnapshot.KpiAnalytics kpi : data.analytics().kpis()) {
            if (kpi.points() != null && kpi.points().size() >= 2) {
                return true;
            }
        }

        return false;
    }

    private byte[] createCorporateWordmark()
            throws IOException {

        int width = 650;
        int height = 255;

        BufferedImage image = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D graphics = image.createGraphics();

        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            java.awt.Font arabic =
                    new java.awt.Font(
                            "Arial",
                            java.awt.Font.BOLD,
                            72
                    );

            TextLayout arabicLayout =
                    new TextLayout(
                            "علف مبروك",
                            arabic,
                            graphics.getFontRenderContext()
                    );

            float arabicX =
                    (width - arabicLayout.getAdvance()) / 2f;

            graphics.setColor(
                    new Color(
                            Byte.toUnsignedInt(LOGO_MAGENTA[0]),
                            Byte.toUnsignedInt(LOGO_MAGENTA[1]),
                            Byte.toUnsignedInt(LOGO_MAGENTA[2])
                    )
            );

            arabicLayout.draw(
                    graphics,
                    arabicX,
                    78f
            );

            java.awt.Font latin =
                    new java.awt.Font(
                            "Arial",
                            java.awt.Font.BOLD,
                            38
                    );

            graphics.setFont(latin);

            graphics.setColor(
                    new Color(
                            Byte.toUnsignedInt(TEXT[0]),
                            Byte.toUnsignedInt(TEXT[1]),
                            Byte.toUnsignedInt(TEXT[2])
                    )
            );

            drawCenteredLetterSpaced(
                    graphics,
                    "ALF MABROUK",
                    width / 2f,
                    145f,
                    3.7f
            );

            java.awt.Font subtitle =
                    new java.awt.Font(
                            "Arial",
                            java.awt.Font.PLAIN,
                            21
                    );

            graphics.setFont(subtitle);

            graphics.setColor(
                    new Color(
                            Byte.toUnsignedInt(LOGO_GREEN[0]),
                            Byte.toUnsignedInt(LOGO_GREEN[1]),
                            Byte.toUnsignedInt(LOGO_GREEN[2])
                    )
            );

            drawCenteredLetterSpaced(
                    graphics,
                    "NUTRITION ANIMALE",
                    width / 2f,
                    201f,
                    5.8f
            );

            graphics.setColor(
                    new Color(
                            Byte.toUnsignedInt(LOGO_MAGENTA[0]),
                            Byte.toUnsignedInt(LOGO_MAGENTA[1]),
                            Byte.toUnsignedInt(LOGO_MAGENTA[2])
                    )
            );

            graphics.fillRect(
                    width / 2 - 43,
                    230,
                    86,
                    4
            );

        } finally {
            graphics.dispose();
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(
                    image,
                    "png",
                    output
            );

            return output.toByteArray();
        }
    }

    private void addWordmark(
            XSSFWorkbook workbook,
            XSSFSheet sheet,
            byte[] wordmark
    ) {
        int pictureIndex = workbook.addPicture(
                wordmark,
                Workbook.PICTURE_TYPE_PNG
        );

        /*
         * Large, dominant wordmark (~380x118 px) anchored at A1, sized to
         * visually fill the A:D / rows 1-4 header area beside the title.
         */
        int heightPixels = 118;
        int widthPixels = 380;

        XSSFClientAnchor anchor =
                new XSSFClientAnchor(
                        0,
                        0,
                        Units.pixelToEMU(widthPixels),
                        Units.pixelToEMU(heightPixels),
                        0,
                        0,
                        0,
                        0
                );

        anchor.setAnchorType(
                ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE
        );

        sheet.createDrawingPatriarch()
                .createPicture(
                        anchor,
                        pictureIndex
                );
    }

    private static void drawCenteredLetterSpaced(
            Graphics2D graphics,
            String text,
            float centerX,
            float baselineY,
            float spacing
    ) {
        FontMetrics metrics = graphics.getFontMetrics();

        float width = 0;

        for (int index = 0; index < text.length(); index++) {
            width += metrics.charWidth(text.charAt(index));

            if (index < text.length() - 1) {
                width += spacing;
            }
        }

        float x = centerX - width / 2f;

        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            String value = String.valueOf(character);

            graphics.drawString(
                    value,
                    x,
                    baselineY
            );

            x += metrics.charWidth(character) + spacing;
        }
    }

    private void summaryCard(
            XSSFSheet sheet,
            int startRow,
            int firstColumn,
            int lastColumn,
            String label,
            String value,
            CardStyles card
    ) {
        mergeText(
                sheet,
                startRow,
                startRow,
                firstColumn,
                lastColumn,
                label.toUpperCase(Locale.FRENCH),
                card.label()
        );

        mergeText(
                sheet,
                startRow + 1,
                startRow + 2,
                firstColumn,
                lastColumn,
                value,
                card.value()
        );

        getOrCreateRow(sheet, startRow).setHeightInPoints(20);
        getOrCreateRow(sheet, startRow + 1).setHeightInPoints(23);
        getOrCreateRow(sheet, startRow + 2).setHeightInPoints(20);
    }

    private void nullableNumeric(
            Row row,
            int column,
            Number value,
            CellStyle numericStyle,
            CellStyle missingStyle
    ) {
        if (value == null) {
            blank(row, column, missingStyle);
        } else {
            numeric(row, column, value, numericStyle);
        }
    }

    private void text(
            Row row,
            int column,
            String value,
            CellStyle style
    ) {
        Cell cell = row.getCell(column);

        if (cell == null) {
            cell = row.createCell(column);
        }

        cell.setCellValue(
                value == null
                        ? ReportDocumentText.MISSING
                        : value
        );

        cell.setCellStyle(style);
    }

    private void numeric(
            Row row,
            int column,
            Number value,
            CellStyle style
    ) {
        Cell cell = row.getCell(column);

        if (cell == null) {
            cell = row.createCell(column);
        }

        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(style);
    }

    private void blank(
            Row row,
            int column,
            CellStyle style
    ) {
        Cell cell = row.getCell(column);

        if (cell == null) {
            cell = row.createCell(column);
        }

        cell.setBlank();
        cell.setCellStyle(style);
    }

    private void mergeText(
            XSSFSheet sheet,
            int firstRow,
            int lastRow,
            int firstColumn,
            int lastColumn,
            String value,
            CellStyle style
    ) {
        Row row = getOrCreateRow(sheet, firstRow);

        Cell cell = row.getCell(firstColumn);

        if (cell == null) {
            cell = row.createCell(firstColumn);
        }

        cell.setCellValue(
                value == null
                        ? ""
                        : value
        );

        cell.setCellStyle(style);

        if (
                firstRow != lastRow
                        || firstColumn != lastColumn
        ) {
            sheet.addMergedRegion(
                    new CellRangeAddress(
                            firstRow,
                            lastRow,
                            firstColumn,
                            lastColumn
                    )
            );

            for (int r = firstRow; r <= lastRow; r++) {
                Row regionRow = getOrCreateRow(sheet, r);

                for (int c = firstColumn; c <= lastColumn; c++) {
                    Cell regionCell = regionRow.getCell(c);

                    if (regionCell == null) {
                        regionCell = regionRow.createCell(c);
                    }

                    regionCell.setCellStyle(style);
                }
            }
        }
    }

    private Row getOrCreateRow(
            XSSFSheet sheet,
            int rowIndex
    ) {
        Row row = sheet.getRow(rowIndex);

        return row == null
                ? sheet.createRow(rowIndex)
                : row;
    }

    private void setColumnWidths(
            XSSFSheet sheet,
            int[] widths
    ) {
        for (int column = 0; column < widths.length; column++) {
            sheet.setColumnWidth(
                    column,
                    widths[column] * 256
            );
        }
    }

    private int findDetailHeaderRow(
            XSSFSheet sheet
    ) {
        for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);

            if (row == null) {
                continue;
            }

            Cell cell = row.getCell(0);

            if (
                    cell != null
                            && cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING
                            && "Date effective".equals(
                            cell.getStringCellValue()
                    )
            ) {
                return rowIndex;
            }
        }

        return 0;
    }

    private String trend(String value) {
        return switch (value) {
            case "INCREASING" -> "Hausse";
            case "DECREASING" -> "Baisse";
            case "STABLE" -> "Stable";
            default -> "Données insuffisantes";
        };
    }

    private String source(String value) {
        return switch (value) {
            case "MANUAL" -> "Saisie manuelle";
            case "PASTE" -> "Texte collé";
            case "GALLERY_OCR" -> "Image importée";
            case "SHARE_OCR" -> "Image partagée";
            default -> value;
        };
    }

    private Styles createStyles(
            XSSFWorkbook workbook
    ) {
        Font titleFont = font(
                workbook,
                "Aptos Display",
                17,
                true,
                TEXT
        );

        Font subtitleFont = font(
                workbook,
                "Aptos",
                11,
                false,
                MUTED
        );

        Font metaFont = font(
                workbook,
                "Aptos",
                9,
                true,
                MUTED
        );

        Font sectionFont = font(
                workbook,
                "Aptos",
                11,
                true,
                TEXT
        );

        Font tableHeaderFont = font(
                workbook,
                "Aptos",
                9,
                true,
                TEXT
        );

        Font bodyFont = font(
                workbook,
                "Aptos",
                10,
                false,
                TEXT
        );

        Font bodyBoldFont = font(
                workbook,
                "Aptos",
                10,
                true,
                TEXT
        );

        Font mutedFont = font(
                workbook,
                "Aptos",
                9,
                false,
                MUTED
        );

        Font mutedItalicFont = font(
                workbook,
                "Aptos",
                9,
                false,
                MUTED
        );

        mutedItalicFont.setItalic(true);

        Font confirmedFont = font(
                workbook,
                "Aptos",
                9,
                true,
                CONFIRMED_GREEN
        );

        Font cardLabelFont = font(
                workbook,
                "Aptos",
                8,
                true,
                MUTED
        );

        Font cardValueFont = font(
                workbook,
                "Aptos Display",
                18,
                true,
                TEXT
        );

        CellStyle title = workbook.createCellStyle();
        title.setFont(titleFont);
        title.setAlignment(HorizontalAlignment.LEFT);
        title.setVerticalAlignment(VerticalAlignment.CENTER);
        title.setWrapText(true);
        fill(title, WARM_WHITE);

        CellStyle subtitle = workbook.createCellStyle();
        subtitle.setFont(subtitleFont);
        subtitle.setAlignment(HorizontalAlignment.LEFT);
        subtitle.setVerticalAlignment(VerticalAlignment.CENTER);
        fill(subtitle, WARM_WHITE);

        CellStyle headerMeta = workbook.createCellStyle();
        headerMeta.setFont(metaFont);
        headerMeta.setAlignment(HorizontalAlignment.LEFT);
        headerMeta.setVerticalAlignment(VerticalAlignment.CENTER);
        fill(headerMeta, WARM_WHITE);

        CellStyle accentLine = workbook.createCellStyle();
        fill(accentLine, SAGE);

        CellStyle section = workbook.createCellStyle();
        section.setFont(sectionFont);
        section.setVerticalAlignment(VerticalAlignment.CENTER);
        fill(section, SECTION_BG);
        section.setBorderBottom(BorderStyle.MEDIUM);
        ((XSSFCellStyle) section).setBottomBorderColor(
                new XSSFColor(SAGE, null)
        );

        CellStyle tableHeader = workbook.createCellStyle();
        tableHeader.setFont(tableHeaderFont);
        tableHeader.setAlignment(HorizontalAlignment.CENTER);
        tableHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        tableHeader.setWrapText(true);
        fill(tableHeader, TABLE_HEADER_BG);
        borders(tableHeader);

        CellStyle body = workbook.createCellStyle();
        body.setFont(bodyFont);
        body.setVerticalAlignment(VerticalAlignment.CENTER);
        body.setWrapText(true);
        borders(body);

        CellStyle bodyBold = workbook.createCellStyle();
        bodyBold.cloneStyleFrom(body);
        bodyBold.setFont(bodyBoldFont);

        CellStyle alternate = workbook.createCellStyle();
        alternate.cloneStyleFrom(body);
        fill(alternate, ALT_ROW);

        CellStyle alternateBold = workbook.createCellStyle();
        alternateBold.cloneStyleFrom(alternate);
        alternateBold.setFont(bodyBoldFont);

        CellStyle decimal = workbook.createCellStyle();
        decimal.cloneStyleFrom(body);
        decimal.setDataFormat(
                workbook.createDataFormat()
                        .getFormat("#,##0.######")
        );

        CellStyle alternateDecimal = workbook.createCellStyle();
        alternateDecimal.cloneStyleFrom(alternate);
        alternateDecimal.setDataFormat(
                workbook.createDataFormat()
                        .getFormat("#,##0.######")
        );

        CellStyle completeness = workbook.createCellStyle();
        completeness.cloneStyleFrom(body);
        completeness.setDataFormat(
                workbook.createDataFormat()
                        .getFormat("0.##\" %\"")
        );

        CellStyle secondaryPercent = workbook.createCellStyle();
        secondaryPercent.cloneStyleFrom(body);
        secondaryPercent.setDataFormat(
                workbook.createDataFormat()
                        .getFormat("#,##0.######\" %\"")
        );

        CellStyle date = workbook.createCellStyle();
        date.cloneStyleFrom(body);
        date.setDataFormat(
                workbook.createDataFormat()
                        .getFormat("dd/mm/yyyy")
        );

        CellStyle alternateDate = workbook.createCellStyle();
        alternateDate.cloneStyleFrom(alternate);
        alternateDate.setDataFormat(
                workbook.createDataFormat()
                        .getFormat("dd/mm/yyyy")
        );

        CellStyle dateTime = workbook.createCellStyle();
        dateTime.cloneStyleFrom(body);
        dateTime.setDataFormat(
                workbook.createDataFormat()
                        .getFormat("dd/mm/yyyy hh:mm")
        );

        CellStyle missing = workbook.createCellStyle();
        missing.cloneStyleFrom(body);
        missing.setFont(mutedItalicFont);
        fill(missing, CARD_PEACH);

        CellStyle notApplicable = workbook.createCellStyle();
        notApplicable.cloneStyleFrom(body);
        notApplicable.setFont(mutedFont);
        fill(notApplicable, ALT_ROW);

        CellStyle confirmed = workbook.createCellStyle();
        confirmed.cloneStyleFrom(body);
        confirmed.setFont(confirmedFont);
        confirmed.setAlignment(HorizontalAlignment.CENTER);
        fill(confirmed, CARD_MINT);

        CellStyle intro = workbook.createCellStyle();
        intro.setFont(bodyFont);
        intro.setVerticalAlignment(VerticalAlignment.CENTER);
        intro.setWrapText(true);

        CellStyle note = workbook.createCellStyle();
        note.setFont(mutedItalicFont);
        note.setWrapText(true);
        note.setVerticalAlignment(VerticalAlignment.CENTER);
        fill(note, WARM_WHITE);

        CellStyle secondaryNote = workbook.createCellStyle();
        secondaryNote.setFont(mutedFont);
        secondaryNote.setWrapText(true);
        secondaryNote.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle metadataLabel = workbook.createCellStyle();
        metadataLabel.setFont(bodyBoldFont);
        metadataLabel.setVerticalAlignment(VerticalAlignment.CENTER);
        fill(metadataLabel, SECTION_BG);

        CellStyle auditFormula = workbook.createCellStyle();
        auditFormula.setFont(bodyBoldFont);
        auditFormula.setAlignment(HorizontalAlignment.LEFT);
        auditFormula.setVerticalAlignment(VerticalAlignment.CENTER);
        fill(auditFormula, SECTION_BG);

        CellStyle footer = workbook.createCellStyle();
        footer.setFont(mutedFont);
        footer.setAlignment(HorizontalAlignment.CENTER);

        CardStyles cardBlue = cardStyles(
                workbook,
                CARD_BLUE,
                cardLabelFont,
                cardValueFont
        );

        CardStyles cardMint = cardStyles(
                workbook,
                CARD_MINT,
                cardLabelFont,
                cardValueFont
        );

        CardStyles cardPeach = cardStyles(
                workbook,
                CARD_PEACH,
                cardLabelFont,
                cardValueFont
        );

        CardStyles cardLavender = cardStyles(
                workbook,
                CARD_LAVENDER,
                cardLabelFont,
                cardValueFont
        );

        return new Styles(
                title,
                subtitle,
                headerMeta,
                accentLine,
                section,
                tableHeader,
                body,
                bodyBold,
                alternate,
                alternateBold,
                decimal,
                alternateDecimal,
                completeness,
                secondaryPercent,
                date,
                alternateDate,
                dateTime,
                missing,
                notApplicable,
                confirmed,
                intro,
                note,
                secondaryNote,
                metadataLabel,
                auditFormula,
                footer,
                cardBlue,
                cardMint,
                cardPeach,
                cardLavender
        );
    }

    private CardStyles cardStyles(
            XSSFWorkbook workbook,
            byte[] fillColor,
            Font labelFont,
            Font valueFont
    ) {
        CellStyle label = workbook.createCellStyle();
        label.setFont(labelFont);
        label.setAlignment(HorizontalAlignment.CENTER);
        label.setVerticalAlignment(VerticalAlignment.CENTER);
        fill(label, fillColor);
        cardBorders(label);

        CellStyle value = workbook.createCellStyle();
        value.setFont(valueFont);
        value.setAlignment(HorizontalAlignment.CENTER);
        value.setVerticalAlignment(VerticalAlignment.CENTER);
        fill(value, fillColor);
        cardBorders(value);

        return new CardStyles(label, value);
    }

    private Font font(
            XSSFWorkbook workbook,
            String name,
            int size,
            boolean bold,
            byte[] colorRgb
    ) {
        XSSFFont font = workbook.createFont();

        font.setFontName(name);
        font.setFontHeightInPoints((short) size);
        font.setBold(bold);
        font.setColor(new XSSFColor(colorRgb, null));

        return font;
    }

    private void fill(
            CellStyle style,
            byte[] rgb
    ) {
        XSSFCellStyle xssf = (XSSFCellStyle) style;

        xssf.setFillForegroundColor(
                new XSSFColor(rgb, null)
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );
    }

    private void borders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);

        XSSFCellStyle xssf = (XSSFCellStyle) style;
        XSSFColor border = new XSSFColor(BORDER, null);

        xssf.setTopBorderColor(border);
        xssf.setRightBorderColor(border);
        xssf.setBottomBorderColor(border);
        xssf.setLeftBorderColor(border);
    }

    private void cardBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);

        XSSFCellStyle xssf = (XSSFCellStyle) style;
        XSSFColor border = new XSSFColor(BORDER, null);

        xssf.setTopBorderColor(border);
        xssf.setRightBorderColor(border);
        xssf.setBottomBorderColor(border);
        xssf.setLeftBorderColor(border);
    }

    private static byte[] rgb(
            int red,
            int green,
            int blue
    ) {
        return new byte[]{
                (byte) red,
                (byte) green,
                (byte) blue
        };
    }

    private record CardStyles(
            CellStyle label,
            CellStyle value
    ) {
    }

    private record Styles(
            CellStyle title,
            CellStyle subtitle,
            CellStyle headerMeta,
            CellStyle accentLine,
            CellStyle section,
            CellStyle tableHeader,
            CellStyle body,
            CellStyle bodyBold,
            CellStyle alternate,
            CellStyle alternateBold,
            CellStyle decimal,
            CellStyle alternateDecimal,
            CellStyle completeness,
            CellStyle secondaryPercent,
            CellStyle date,
            CellStyle alternateDate,
            CellStyle dateTime,
            CellStyle missing,
            CellStyle notApplicable,
            CellStyle confirmed,
            CellStyle intro,
            CellStyle note,
            CellStyle secondaryNote,
            CellStyle metadataLabel,
            CellStyle auditFormula,
            CellStyle footer,
            CardStyles cardBlue,
            CardStyles cardMint,
            CardStyles cardPeach,
            CardStyles cardLavender
    ) {
    }
}