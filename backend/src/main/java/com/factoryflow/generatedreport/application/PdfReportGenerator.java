package com.factoryflow.generatedreport.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

@Component
public class PdfReportGenerator {

    private static final PDType1Font REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final float LEFT = 42;
    private static final float ROW_HEIGHT = 18;

    public byte[] generate(ReportGenerationData data) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDDocumentInformation info = new PDDocumentInformation();
            info.setTitle("FactoryFlow Maintenance KPI Report");
            info.setSubject(data.type() + " report for " + data.period().start() + " to " + data.period().end());
            info.setCreator("FactoryFlow");
            document.setDocumentInformation(info);

            PageWriter writer = newPage(document, data);
            for (ReportGenerationData.Row row : data.rows()) {
                if (writer.y < 120) {
                    writer.close();
                    writer = newPage(document, data);
                }
                writer.row(row);
            }
            if (data.rows().isEmpty()) writer.message("No confirmed maintenance data exists for this period.");
            writer.close();
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("PDF generation failed", exception);
        }
    }

    private PageWriter newPage(PDDocument document, ReportGenerationData data) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream stream = new PDPageContentStream(document, page);
        PageWriter writer = new PageWriter(stream, page.getMediaBox().getHeight() - 46);
        writer.text(BOLD, 17, "FactoryFlow Maintenance KPI Report");
        writer.gap(8);
        writer.text(REGULAR, 10, "Report type: " + data.type());
        writer.text(REGULAR, 10, "Reporting period: " + data.period().start() + " to " + data.period().end());
        writer.text(REGULAR, 10, "Generated at: " + data.generatedAt());
        writer.gap(10);
        writer.text(BOLD, 9, String.format("%-12s %-8s %-18s", "Date", "Report", "Confirmed value"));
        writer.line();
        return writer;
    }

    private static List<String> wrap(String value, int max) {
        String remaining = value == null || value.isBlank() ? "-" : value.replace('\n', ' ').replace('\r', ' ');
        List<String> lines = new ArrayList<>();
        while (remaining.length() > max) {
            int split = remaining.lastIndexOf(' ', max);
            if (split < 1) split = max;
            lines.add(remaining.substring(0, split));
            remaining = remaining.substring(split).stripLeading();
        }
        lines.add(remaining);
        return lines;
    }

    private static final class PageWriter implements AutoCloseable {
        private final PDPageContentStream stream;
        private float y;

        private PageWriter(PDPageContentStream stream, float y) { this.stream = stream; this.y = y; }

        private void text(PDType1Font font, float size, String text) throws IOException {
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(LEFT, y);
            stream.showText(text.replace('…', '.'));
            stream.endText();
            y -= size + 4;
        }

        private void row(ReportGenerationData.Row row) throws IOException {
            String value = row.confirmedValue() == null ? "Missing" : row.confirmedValue().stripTrailingZeros().toPlainString();
            text(REGULAR, 8.5f, String.format(Locale.ROOT, "%-12s %-8s %-18s",
                    row.effectiveDate(), row.sourceReportId(), value));
            for (String line : wrap("KPI: " + row.kpiName() + " | Unit: "
                    + (row.unit() == null ? "-" : row.unit()) + " | Source: " + row.source(), 100)) {
                text(REGULAR, 8, "  " + line);
            }
            y -= Math.max(0, ROW_HEIGHT - 12.5f);
        }

        private void message(String message) throws IOException { text(REGULAR, 10, message); }
        private void gap(float amount) { y -= amount; }
        private void line() throws IOException {
            stream.moveTo(LEFT, y + 5);
            stream.lineTo(PDRectangle.A4.getWidth() - LEFT, y + 5);
            stream.stroke();
            y -= 4;
        }
        @Override public void close() throws IOException { stream.close(); }
    }
}
