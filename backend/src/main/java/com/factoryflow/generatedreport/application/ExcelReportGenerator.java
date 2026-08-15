package com.factoryflow.generatedreport.application;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xddf.usermodel.chart.*;
import org.springframework.stereotype.Component;

@Component
public class ExcelReportGenerator {

    private static final String LOGO_RESOURCE = "/reporting/alf-mabrouk-logo.png";

    private static final String[] HEADERS = {
            "Date effective", "Indicateur", "Valeur confirmée", "Valeur associée", "Unité", "Statut", "Rapport source", "Confirmé le"
    };
    private static final byte[] MAGENTA = {(byte) 156, 27, 110};
    private static final byte[] GREEN = {(byte) 91, (byte) 154, 47};
    private static final byte[] CREAM = {(byte) 251, (byte) 247, (byte) 241};
    private static final byte[] INK = {(byte) 36, 27, 34};

    public byte[] generate(ReportGenerationData data) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Styles styles = createStyles(workbook);
            byte[] officialLogo = logoBytes();
            createSummary(workbook, data, styles, officialLogo);
            createIndicators(workbook, data, styles, officialLogo);
            createAnalysis(workbook, data, styles);
            createQuality(workbook, data, styles);
            createTraceability(workbook, data, styles);
            workbook.setActiveSheet(0);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Excel workbook generation failed", exception);
        }
    }

    private byte[] logoBytes() throws IOException {
        try (InputStream input = ExcelReportGenerator.class.getResourceAsStream(LOGO_RESOURCE)) {
            if (input == null) throw new IOException("Official Alf Mabrouk logo resource is missing");
            return input.readAllBytes();
        }
    }

    private void createSummary(XSSFWorkbook workbook, ReportGenerationData data, Styles styles, byte[] logoBytes) throws IOException {
        XSSFSheet sheet = workbook.createSheet("Synthèse");
        sheet.setDisplayGridlines(false);
        sheet.setPrintGridlines(false);
        sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
        sheet.getPrintSetup().setFitWidth((short) 1);
        sheet.setFitToPage(true);
        sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.LeftMargin, .45);
        sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.RightMargin, .45);

        addOfficialLogo(workbook, sheet, logoBytes);
        mergeText(sheet, 0, 0, 3, 7, ReportDocumentText.title(data.type()), styles.title());
        sheet.getRow(0).setHeightInPoints(40);
        mergeText(sheet, 1, 1, 3, 7, ReportDocumentText.PRODUCT + " · Données de maintenance confirmées", styles.subtitle());
        mergeText(sheet, 3, 3, 0, 7,
                "Ce document synthétise les indicateurs de maintenance confirmés pour la période sélectionnée.",
                styles.intro());

        metadata(sheet, 5, "Période", ReportDocumentText.period(data), styles);
        metadata(sheet, 6, "Généré le", ReportDocumentText.instant(data.generatedAt()), styles);
        metadata(sheet, 7, "Statut des données", ReportDocumentText.CONFIRMED, styles);
        metadata(sheet, 8, "Validé par", submitters(data), styles);

        long reports = data.rows().stream().map(ReportGenerationData.Row::sourceReportId).distinct().count();
        long missing = data.rows().stream().filter(row -> row.confirmedValue() == null).count();
        summaryCard(sheet, 10, 0, 1, "Rapports sources", reports, styles);
        summaryCard(sheet, 10, 2, 3, "Indicateurs", data.rows().size(), styles);
        summaryCard(sheet, 10, 4, 7, "Non renseignés", missing, styles);

        mergeText(sheet, 13, 13, 0, 7,
                "Les valeurs non renseignées sont conservées comme telles et ne sont jamais converties en zéro.",
                styles.note());
        mergeText(sheet, 16, 16, 0, 7,
                "Alf Mabrouk · " + ReportDocumentText.PRODUCT + " · Document généré automatiquement",
                styles.footer());

        int[] widths = {20, 18, 18, 18, 18, 18, 18, 20};
        for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i] * 256);
    }

    private void addOfficialLogo(XSSFWorkbook workbook, XSSFSheet sheet, byte[] logoBytes) throws IOException {
        int pictureIndex = workbook.addPicture(logoBytes, Workbook.PICTURE_TYPE_PNG);
        var source = ImageIO.read(new ByteArrayInputStream(logoBytes));
        int heightPixels = 64;
        int widthPixels = Math.round(heightPixels * (source.getWidth() / (float) source.getHeight()));
        XSSFClientAnchor anchor = new XSSFClientAnchor(
                0, 0, Units.pixelToEMU(widthPixels), Units.pixelToEMU(heightPixels), 0, 0, 0, 0);
        anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
        sheet.createDrawingPatriarch().createPicture(anchor, pictureIndex);
    }

    private void createIndicators(XSSFWorkbook workbook, ReportGenerationData data, Styles styles, byte[] logoBytes) throws IOException {
        XSSFSheet sheet = workbook.createSheet("Données");
        sheet.setDisplayGridlines(false);
        sheet.setPrintGridlines(false);
        sheet.getPrintSetup().setLandscape(true);
        sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
        sheet.getPrintSetup().setFitWidth((short) 1);
        sheet.setFitToPage(true);
        sheet.setRepeatingRows(new CellRangeAddress(5, 5, -1, -1));

        addOfficialLogo(workbook, sheet, logoBytes);
        mergeText(sheet, 0, 0, 3, 7, ReportDocumentText.title(data.type()), styles.title());
        sheet.getRow(0).setHeightInPoints(48);
        mergeText(sheet, 1, 1, 0, 7, ReportDocumentText.period(data) + " · " + ReportDocumentText.CONFIRMED, styles.subtitle());
        mergeText(sheet, 3, 3, 0, 7, "Détail des indicateurs confirmés", styles.section());

        Row header = sheet.createRow(5);
        header.setHeightInPoints(28);
        for (int column = 0; column < HEADERS.length; column++) {
            Cell cell = header.createCell(column);
            cell.setCellValue(HEADERS[column]);
            cell.setCellStyle(styles.header());
        }

        int rowIndex = 6;
        for (ReportGenerationData.Row value : data.rows()) {
            Row row = sheet.createRow(rowIndex);
            row.setHeightInPoints(24);
            boolean missing = value.confirmedValue() == null;
            CellStyle body = rowIndex % 2 == 0 ? styles.body() : styles.alternate();
            text(row, 0, ReportDocumentText.date(value.effectiveDate()), body);
            text(row, 1, value.kpiName(), body);
            if (missing) blank(row, 2, styles.missing()); else numeric(row, 2, value.confirmedValue(), styles.decimal());
            if (value.secondaryConfirmedValue() == null) text(row, 3, "Sans objet", styles.notApplicable());
            else numeric(row, 3, value.secondaryConfirmedValue(), styles.decimal());
            text(row, 4, ReportDocumentText.unit(value.unit()), body);
            text(row, 5, ReportDocumentText.CONFIRMED, styles.confirmed());
            text(row, 6, "N°" + value.sourceReportId(), body);
            text(row, 7, ReportDocumentText.instant(value.confirmedAt()), body);
            rowIndex++;
        }
        if (data.rows().isEmpty()) {
            mergeText(sheet, 6, 7, 0, 7, "Aucune donnée de maintenance confirmée pour cette période.", styles.note());
            rowIndex = 8;
        }

        mergeText(sheet, rowIndex + 2, rowIndex + 2, 0, 7,
                "Alf Mabrouk · " + ReportDocumentText.PRODUCT + " · Généré le " + ReportDocumentText.instant(data.generatedAt()),
                styles.footer());
        sheet.createFreezePane(0, 6);
        sheet.setAutoFilter(new CellRangeAddress(5, Math.max(5, rowIndex - 1), 0, HEADERS.length - 1));
        if (rowIndex > 6) {
            var conditional = sheet.getSheetConditionalFormatting();
            var missingRule = conditional.createConditionalFormattingRule("ISBLANK(C7)");
            var pattern = missingRule.createPatternFormatting();
            pattern.setFillBackgroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
            pattern.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
            conditional.addConditionalFormatting(
                    new CellRangeAddress[]{new CellRangeAddress(6, rowIndex - 1, 2, 2)}, missingRule);
        }
        int[] widths = {19, 30, 20, 20, 16, 16, 18, 27};
        for (int column = 0; column < widths.length; column++) sheet.setColumnWidth(column, widths[column] * 256);
    }

    private void createAnalysis(XSSFWorkbook workbook, ReportGenerationData data, Styles styles) {
        XSSFSheet sheet = workbook.createSheet("Analyse KPI");
        sheet.setDisplayGridlines(false);
        String[] headers = {"Indicateur", "Unité", "Dernière", "Moyenne", "Minimum", "Maximum", "Étendue", "Écart type", "Évolution", "Complétude %"};
        Row header = sheet.createRow(0);
        for (int column = 0; column < headers.length; column++) text(header, column, headers[column], styles.header());
        int rowIndex = 1;
        for (var kpi : data.analytics().kpis()) {
            Row row = sheet.createRow(rowIndex++);
            text(row, 0, kpi.displayName(), styles.body());
            text(row, 1, ReportDocumentText.unit(kpi.unit()), styles.body());
            nullableNumeric(row, 2, kpi.latest(), styles);
            nullableNumeric(row, 3, kpi.mean(), styles);
            nullableNumeric(row, 4, kpi.minimum(), styles);
            nullableNumeric(row, 5, kpi.maximum(), styles);
            nullableNumeric(row, 6, kpi.range(), styles);
            nullableNumeric(row, 7, kpi.standardDeviation(), styles);
            text(row, 8, trend(kpi.trend().name()), styles.body());
            nullableNumeric(row, 9, kpi.completenessRate(), styles);
        }
        sheet.createFreezePane(0, 1);
        if (rowIndex > 1) {
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFChart chart = drawing.createChart(drawing.createAnchor(0, 0, 0, 0, 11, 1, 20, 18));
            chart.setTitleText("Moyenne confirmée par indicateur");
            chart.setTitleOverlay(false);
            XDDFCategoryAxis categories = chart.createCategoryAxis(AxisPosition.BOTTOM);
            XDDFValueAxis values = chart.createValueAxis(AxisPosition.LEFT);
            values.setCrosses(AxisCrosses.AUTO_ZERO);
            XDDFDataSource<String> labels = XDDFDataSourcesFactory.fromStringCellRange(sheet, new CellRangeAddress(1, rowIndex - 1, 0, 0));
            XDDFNumericalDataSource<Double> averages = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(1, rowIndex - 1, 3, 3));
            XDDFChartData chartData = chart.createData(ChartTypes.BAR, categories, values);
            chartData.setVaryColors(false);
            XDDFChartData.Series series = chartData.addSeries(labels, averages);
            series.setTitle("Moyenne", null);
            chart.plot(chartData);
        }
        for (int column = 0; column < headers.length; column++) sheet.setColumnWidth(column, (column == 0 ? 28 : 16) * 256);
    }

    private void createQuality(XSSFWorkbook workbook, ReportGenerationData data, Styles styles) {
        XSSFSheet sheet = workbook.createSheet("Qualité des données");
        sheet.setDisplayGridlines(false);
        metadata(sheet, 0, "Rapports confirmés", Long.toString(data.analytics().reportCount()), styles);
        metadata(sheet, 1, "Mesures confirmées", Long.toString(data.analytics().measurementCount()), styles);
        metadata(sheet, 2, "Valeurs non renseignées", Long.toString(data.analytics().missingValueCount()), styles);
        metadata(sheet, 3, "Complétude", data.analytics().completenessRate() == null ? ReportDocumentText.MISSING : data.analytics().completenessRate() + " %", styles);
        mergeText(sheet, 5, 5, 0, 7, "Une valeur non renseignée reste vide dans la feuille Données et est exclue des calculs.", styles.note());
        metadata(sheet, 7, "Contrôle Excel — cellules numériques", "", styles);
        Cell formula = sheet.getRow(7).getCell(2);
        int lastDataRow = 6 + data.rows().size();
        formula.setCellFormula("COUNT('Données'!C7:C" + Math.max(7, lastDataRow) + ")");
        formula.setCellStyle(styles.body());
        for (int column = 0; column < 8; column++) sheet.setColumnWidth(column, 20 * 256);
    }

    private void createTraceability(XSSFWorkbook workbook, ReportGenerationData data, Styles styles) {
        XSSFSheet sheet = workbook.createSheet("Traçabilité");
        sheet.setDisplayGridlines(false);
        String[] headers = {"Rapport source", "Date effective", "Source", "Soumis par", "Confirmé le"};
        Row header = sheet.createRow(0);
        for (int column = 0; column < headers.length; column++) text(header, column, headers[column], styles.header());
        int rowIndex = 1;
        Set<Long> seen = new LinkedHashSet<>();
        for (ReportGenerationData.Row value : data.rows()) {
            if (!seen.add(value.sourceReportId())) continue;
            Row row = sheet.createRow(rowIndex++);
            numeric(row, 0, value.sourceReportId(), styles.body());
            Cell dateCell = row.createCell(1); dateCell.setCellValue(value.effectiveDate()); dateCell.setCellStyle(styles.date());
            text(row, 2, source(value.source().name()), styles.body());
            text(row, 3, value.submittedBy(), styles.body());
            text(row, 4, ReportDocumentText.instant(value.confirmedAt()), styles.body());
        }
        sheet.createFreezePane(0, 1);
        if (rowIndex > 1) sheet.setAutoFilter(new CellRangeAddress(0, rowIndex - 1, 0, headers.length - 1));
        for (int column = 0; column < headers.length; column++) sheet.setColumnWidth(column, 24 * 256);
    }

    private void nullableNumeric(Row row, int column, Number value, Styles styles) {
        if (value == null) blank(row, column, styles.missing()); else numeric(row, column, value, styles.decimal());
    }

    private void blank(Row row, int column, CellStyle style) { Cell cell = row.createCell(column); cell.setBlank(); cell.setCellStyle(style); }

    private String trend(String value) { return switch (value) {
        case "INCREASING" -> "Hausse"; case "DECREASING" -> "Baisse"; case "STABLE" -> "Stable"; default -> "Données insuffisantes";
    }; }

    private String source(String value) { return switch (value) {
        case "MANUAL" -> "Saisie manuelle"; case "PASTE" -> "Texte collé"; case "GALLERY_OCR" -> "Image importée"; case "SHARE_OCR" -> "Image partagée"; default -> value;
    }; }

    private String submitters(ReportGenerationData data) {
        Set<String> names = new LinkedHashSet<>();
        data.rows().stream().map(ReportGenerationData.Row::submittedBy).filter(name -> name != null && !name.isBlank())
                .forEach(names::add);
        return names.isEmpty() ? ReportDocumentText.MISSING : String.join(", ", names);
    }

    private void summaryCard(XSSFSheet sheet, int rowIndex, int firstColumn, int lastColumn, String label, long value,
                             Styles styles) {
        mergeText(sheet, rowIndex, rowIndex, firstColumn, lastColumn, label, styles.cardLabel());
        mergeText(sheet, rowIndex + 1, rowIndex + 1, firstColumn, lastColumn, Long.toString(value), styles.cardValue());
    }

    private void metadata(XSSFSheet sheet, int rowIndex, String label, String value, Styles styles) {
        mergeText(sheet, rowIndex, rowIndex, 0, 1, label, styles.metadataLabel());
        mergeText(sheet, rowIndex, rowIndex, 2, 7, value, styles.body());
    }

    private void mergeText(XSSFSheet sheet, int firstRow, int lastRow, int firstColumn, int lastColumn, String value,
                           CellStyle style) {
        Row row = sheet.getRow(firstRow) == null ? sheet.createRow(firstRow) : sheet.getRow(firstRow);
        Cell cell = row.createCell(firstColumn);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        if (firstRow != lastRow || firstColumn != lastColumn) {
            sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstColumn, lastColumn));
            for (int r = firstRow; r <= lastRow; r++) {
                Row regionRow = sheet.getRow(r) == null ? sheet.createRow(r) : sheet.getRow(r);
                for (int c = firstColumn; c <= lastColumn; c++) {
                    Cell regionCell = regionRow.getCell(c) == null ? regionRow.createCell(c) : regionRow.getCell(c);
                    regionCell.setCellStyle(style);
                }
            }
        }
    }

    private void text(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value == null ? ReportDocumentText.MISSING : value);
        cell.setCellStyle(style);
    }

    private void numeric(Row row, int column, Number value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(style);
    }

    private Styles createStyles(XSSFWorkbook workbook) {
        CellStyle brand = colored(workbook, MAGENTA, true, 15, IndexedColors.WHITE.getIndex());
        brand.setAlignment(HorizontalAlignment.LEFT);
        CellStyle title = colored(workbook, INK, true, 15, IndexedColors.WHITE.getIndex());
        title.setAlignment(HorizontalAlignment.RIGHT);
        CellStyle subtitle = colored(workbook, INK, false, 10, IndexedColors.WHITE.getIndex());
        subtitle.setAlignment(HorizontalAlignment.RIGHT);
        CellStyle section = colored(workbook, MAGENTA, true, 11, IndexedColors.WHITE.getIndex());
        section.setAlignment(HorizontalAlignment.LEFT);
        CellStyle header = colored(workbook, MAGENTA, true, 10, IndexedColors.WHITE.getIndex());
        header.setAlignment(HorizontalAlignment.CENTER);
        borders(header);

        Font bold = workbook.createFont(); bold.setBold(true);
        CellStyle metadataLabel = workbook.createCellStyle(); metadataLabel.setFont(bold); metadataLabel.setVerticalAlignment(VerticalAlignment.CENTER);
        CellStyle body = workbook.createCellStyle(); borders(body); body.setVerticalAlignment(VerticalAlignment.CENTER);
        CellStyle alternate = workbook.createCellStyle(); alternate.cloneStyleFrom(body); fill(alternate, CREAM);
        CellStyle decimal = workbook.createCellStyle(); decimal.cloneStyleFrom(body);
        decimal.setDataFormat(workbook.createDataFormat().getFormat("#,##0.######"));
        CellStyle date = workbook.createCellStyle(); date.cloneStyleFrom(body);
        date.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));
        CellStyle missing = workbook.createCellStyle(); missing.cloneStyleFrom(body); fill(missing, new byte[]{(byte) 235, (byte) 232, (byte) 230});
        Font missingFont = workbook.createFont(); missingFont.setItalic(true); missingFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex()); missing.setFont(missingFont);
        CellStyle notApplicable = workbook.createCellStyle(); notApplicable.cloneStyleFrom(body); notApplicable.setFont(missingFont);
        CellStyle confirmed = workbook.createCellStyle(); confirmed.cloneStyleFrom(body); fill(confirmed, new byte[]{(byte) 230, (byte) 241, (byte) 222});
        Font confirmedFont = workbook.createFont(); confirmedFont.setBold(true); confirmedFont.setColor(IndexedColors.DARK_GREEN.getIndex()); confirmed.setFont(confirmedFont);
        CellStyle intro = workbook.createCellStyle(); intro.setWrapText(true); intro.setVerticalAlignment(VerticalAlignment.CENTER);
        CellStyle note = workbook.createCellStyle(); note.setWrapText(true); fill(note, new byte[]{(byte) 245, (byte) 242, (byte) 240});
        Font noteFont = workbook.createFont(); noteFont.setItalic(true); noteFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex()); note.setFont(noteFont);
        CellStyle footer = workbook.createCellStyle(); footer.setAlignment(HorizontalAlignment.CENTER); footer.setFont(noteFont);
        CellStyle cardLabel = colored(workbook, GREEN, true, 10, IndexedColors.WHITE.getIndex()); cardLabel.setAlignment(HorizontalAlignment.CENTER);
        CellStyle cardValue = workbook.createCellStyle(); cardValue.setAlignment(HorizontalAlignment.CENTER);
        Font cardFont = workbook.createFont(); cardFont.setBold(true); cardFont.setFontHeightInPoints((short) 18); cardValue.setFont(cardFont); fill(cardValue, CREAM);
        return new Styles(brand, title, subtitle, section, header, metadataLabel, body, alternate, decimal, missing, notApplicable,
                confirmed, intro, note, footer, cardLabel, cardValue, date);
    }

    private CellStyle colored(XSSFWorkbook workbook, byte[] rgb, boolean bold, int size, short fontColor) {
        Font font = workbook.createFont(); font.setBold(bold); font.setFontHeightInPoints((short) size); font.setColor(fontColor);
        CellStyle style = workbook.createCellStyle(); style.setFont(font); fill(style, rgb); style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private void fill(CellStyle style, byte[] rgb) {
        ((org.apache.poi.xssf.usermodel.XSSFCellStyle) style).setFillForegroundColor(new XSSFColor(rgb, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    private void borders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN); style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN); style.setBorderLeft(BorderStyle.THIN);
    }

    private record Styles(CellStyle brand, CellStyle title, CellStyle subtitle, CellStyle section, CellStyle header,
                          CellStyle metadataLabel, CellStyle body, CellStyle alternate, CellStyle decimal,
                          CellStyle missing, CellStyle notApplicable, CellStyle confirmed, CellStyle intro, CellStyle note, CellStyle footer,
                          CellStyle cardLabel, CellStyle cardValue, CellStyle date) { }
}
