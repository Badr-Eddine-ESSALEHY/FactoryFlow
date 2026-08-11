package com.factoryflow.generatedreport.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class ExcelReportGenerator {

    private static final String[] HEADERS = {
            "Effective date", "Source report", "Acquisition source", "KPI", "Unit",
            "Confirmed value", "Submitted by", "Confirmed at"
    };

    public byte[] generate(ExcelReportData data) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Maintenance KPIs");
            Styles styles = createStyles(workbook);

            Row title = sheet.createRow(0);
            title.createCell(0).setCellValue("FactoryFlow Maintenance KPI Report");
            title.getCell(0).setCellStyle(styles.title());
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            metadataRow(sheet, 1, "Report type", data.type().name(), styles);
            metadataRow(sheet, 2, "Reporting period", data.period().start() + " to " + data.period().end(), styles);
            metadataRow(sheet, 3, "Generated at", data.generatedAt().toString(), styles);

            Row header = sheet.createRow(5);
            for (int column = 0; column < HEADERS.length; column++) {
                Cell cell = header.createCell(column);
                cell.setCellValue(HEADERS[column]);
                cell.setCellStyle(styles.header());
            }

            int rowIndex = 6;
            for (ExcelReportData.Row value : data.rows()) {
                Row row = sheet.createRow(rowIndex++);
                text(row, 0, value.effectiveDate().toString(), styles.body());
                numeric(row, 1, value.sourceReportId(), styles.integer());
                text(row, 2, value.source().name(), styles.body());
                text(row, 3, value.kpiName(), styles.body());
                text(row, 4, value.unit(), styles.body());
                if (value.confirmedValue() == null) {
                    text(row, 5, "Missing", styles.missing());
                } else {
                    numeric(row, 5, value.confirmedValue().doubleValue(), styles.decimal());
                }
                text(row, 6, value.submittedBy(), styles.body());
                text(row, 7, value.confirmedAt() == null ? null : value.confirmedAt().toString(), styles.body());
            }

            sheet.createFreezePane(0, 6);
            sheet.setAutoFilter(new CellRangeAddress(5, Math.max(5, rowIndex - 1), 0, HEADERS.length - 1));
            int[] widths = {14, 15, 20, 26, 12, 18, 24, 27};
            for (int column = 0; column < widths.length; column++) {
                sheet.setColumnWidth(column, widths[column] * 256);
            }
            sheet.setPrintGridlines(false);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Excel workbook generation failed", exception);
        }
    }

    private void metadataRow(Sheet sheet, int index, String label, String value, Styles styles) {
        Row row = sheet.createRow(index);
        text(row, 0, label, styles.metadataLabel());
        text(row, 1, value, styles.body());
        sheet.addMergedRegion(new CellRangeAddress(index, index, 1, HEADERS.length - 1));
    }

    private void text(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void numeric(Row row, int column, Number value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(style);
    }

    private Styles createStyles(XSSFWorkbook workbook) {
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle title = workbook.createCellStyle();
        title.setFont(titleFont);
        title.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        title.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        title.setAlignment(HorizontalAlignment.CENTER);

        Font bold = workbook.createFont();
        bold.setBold(true);
        CellStyle metadataLabel = workbook.createCellStyle();
        metadataLabel.setFont(bold);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle header = workbook.createCellStyle();
        header.setFont(headerFont);
        header.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        header.setAlignment(HorizontalAlignment.CENTER);
        borders(header);

        CellStyle body = workbook.createCellStyle();
        borders(body);
        CellStyle integer = workbook.createCellStyle();
        integer.cloneStyleFrom(body);
        integer.setDataFormat(workbook.createDataFormat().getFormat("0"));
        CellStyle decimal = workbook.createCellStyle();
        decimal.cloneStyleFrom(body);
        decimal.setDataFormat(workbook.createDataFormat().getFormat("0.######"));
        CellStyle missing = workbook.createCellStyle();
        missing.cloneStyleFrom(body);
        missing.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        missing.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return new Styles(title, metadataLabel, header, body, integer, decimal, missing);
    }

    private void borders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }

    private record Styles(CellStyle title, CellStyle metadataLabel, CellStyle header, CellStyle body,
                          CellStyle integer, CellStyle decimal, CellStyle missing) { }
}
