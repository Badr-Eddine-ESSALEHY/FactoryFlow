package com.factoryflow.generatedreport.application;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

@Component
public class PdfReportGenerator {

    private static final String LOGO_RESOURCE = "/reporting/alf-mabrouk-logo.png";

    private static final PDType1Font REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDType1Font ITALIC = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
    private static final Color MAGENTA = new Color(156, 27, 110);
    private static final Color GREEN = new Color(91, 154, 47);
    private static final Color BLUE = new Color(53, 111, 245);
    private static final Color INK = new Color(36, 27, 34);
    private static final Color MUTED = new Color(112, 103, 109);
    private static final Color CREAM = new Color(251, 247, 241);
    private static final Color BORDER = new Color(218, 212, 215);
    private static final float LEFT = 42;
    private static final float RIGHT = PDRectangle.A4.getWidth() - 42;
    private static final float WIDTH = RIGHT - LEFT;

    public byte[] generate(ReportGenerationData data) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDDocumentInformation info = new PDDocumentInformation();
            info.setTitle(ReportDocumentText.title(data.type()));
            info.setSubject("Indicateurs de maintenance confirmés · " + ReportDocumentText.period(data));
            info.setAuthor(ReportDocumentText.BRAND);
            info.setCreator(ReportDocumentText.PRODUCT);
            document.setDocumentInformation(info);

            PDImageXObject logo = PDImageXObject.createFromByteArray(document, logoBytes(), "Logo officiel Alf Mabrouk");
            PageWriter writer = newPage(document, data, logo, true);
            int index = 0;
            for (ReportGenerationData.Row row : data.rows()) {
                if (!writer.canFitRow()) {
                    writer.close();
                    writer = newPage(document, data, logo, false);
                }
                writer.row(row, index++ % 2 == 1);
            }
            if (data.rows().isEmpty()) writer.emptyMessage();
            writer.close();
            addFooters(document, data);
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("PDF generation failed", exception);
        }
    }

    private byte[] logoBytes() throws IOException {
        try (InputStream input = PdfReportGenerator.class.getResourceAsStream(LOGO_RESOURCE)) {
            if (input == null) throw new IOException("Official Alf Mabrouk logo resource is missing");
            return input.readAllBytes();
        }
    }

    private PageWriter newPage(PDDocument document, ReportGenerationData data, PDImageXObject logo, boolean firstPage) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PageWriter writer = new PageWriter(new PDPageContentStream(document, page), data, logo);
        writer.header(firstPage);
        return writer;
    }

    private void addFooters(PDDocument document, ReportGenerationData data) throws IOException {
        int total = document.getNumberOfPages();
        for (int i = 0; i < total; i++) {
            PDPage page = document.getPage(i);
            try (PDPageContentStream stream = new PDPageContentStream(
                    document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                stream.setStrokingColor(BORDER);
                stream.moveTo(LEFT, 42); stream.lineTo(RIGHT, 42); stream.stroke();
                drawText(stream, REGULAR, 8, MUTED, LEFT, 27,
                        "Alf Mabrouk · FactoryFlow · Généré le " + ReportDocumentText.instant(data.generatedAt()));
                String pageText = "Page " + (i + 1) + " / " + total;
                drawText(stream, BOLD, 8, MUTED, RIGHT - textWidth(BOLD, 8, pageText), 27, pageText);
            }
        }
    }

    private static final class PageWriter implements AutoCloseable {
        private final PDPageContentStream stream;
        private final ReportGenerationData data;
        private final PDImageXObject logo;
        private float y = PDRectangle.A4.getHeight() - 42;

        private PageWriter(PDPageContentStream stream, ReportGenerationData data, PDImageXObject logo) {
            this.stream = stream;
            this.data = data;
            this.logo = logo;
        }

        private void header(boolean firstPage) throws IOException {
            float logoWidth = 88;
            float logoHeight = logoWidth * logo.getHeight() / logo.getWidth();
            stream.drawImage(logo, LEFT, y - logoHeight, logoWidth, logoHeight);

            String title = ReportDocumentText.title(data.type()).toUpperCase(java.util.Locale.FRENCH);
            drawRight(BOLD, 14, INK, title, y - 10);
            drawRight(REGULAR, 9.5f, MUTED, ReportDocumentText.period(data), y - 27);
            y -= Math.max(66, logoHeight + 6);
            stream.setNonStrokingColor(MAGENTA); stream.addRect(LEFT, y, WIDTH * .72f, 3); stream.fill();
            stream.setNonStrokingColor(GREEN); stream.addRect(LEFT + WIDTH * .72f, y, WIDTH * .28f, 3); stream.fill();
            y -= firstPage ? 24 : 18;

            if (firstPage) {
                drawText(stream, REGULAR, 9.5f, INK, LEFT, y,
                        "Ce rapport présente les indicateurs de maintenance confirmés pour la période sélectionnée.");
                y -= 24;
                summary();
                y -= 18;
                if (data.type() != com.factoryflow.generatedreport.domain.GeneratedReportType.DAILY) {
                    executiveAnalysis();
                    y -= 18;
                }
                traceability();
                y -= 18;
            }
            sectionTitle("INDICATEURS CONFIRMÉS");
            tableHeader();
        }

        private void executiveAnalysis() throws IOException {
            sectionTitle("SYNTHÈSE EXÉCUTIVE");
            String quality = data.analytics().completenessRate() == null ? ReportDocumentText.MISSING
                    : data.analytics().completenessRate().stripTrailingZeros().toPlainString() + " %";
            drawText(stream, REGULAR, 8.5f, INK, LEFT + 8, y - 15,
                    "Complétude des données : " + quality + " · " + data.analytics().measurementCount() + " mesures confirmées");
            y -= 26;
            for (var kpi : data.analytics().kpis().stream().limit(4).toList()) {
                String mean = kpi.mean() == null ? ReportDocumentText.MISSING : ReportDocumentText.value(kpi.mean());
                String trend = switch (kpi.trend()) {
                    case INCREASING -> "hausse";
                    case DECREASING -> "baisse";
                    case STABLE -> "stable";
                    case INSUFFICIENT_DATA -> "données insuffisantes";
                };
                drawText(stream, BOLD, 8, INK, LEFT + 8, y - 12, kpi.displayName());
                drawText(stream, REGULAR, 8, MUTED, LEFT + 185, y - 12,
                        "Moyenne " + mean + " " + ReportDocumentText.unit(kpi.unit()) + " · Tendance " + trend);
                y -= 19;
            }
            data.analytics().kpis().stream().filter(kpi -> kpi.points().size() >= 2).findFirst()
                    .ifPresent(kpi -> {
                        try { trendChart(kpi); } catch (IOException exception) { throw new java.io.UncheckedIOException(exception); }
                    });
        }

        private void trendChart(com.factoryflow.analytics.domain.AnalyticsSnapshot.KpiAnalytics kpi) throws IOException {
            float chartHeight = 52;
            float chartWidth = WIDTH - 18;
            float bottom = y - chartHeight - 19;
            drawText(stream, BOLD, 7.5f, MUTED, LEFT + 8, y - 7, "ÉVOLUTION — " + kpi.displayName().toUpperCase(java.util.Locale.FRENCH));
            double minimum = kpi.points().stream().mapToDouble(point -> point.value().doubleValue()).min().orElse(0);
            double maximum = kpi.points().stream().mapToDouble(point -> point.value().doubleValue()).max().orElse(minimum + 1);
            double range = Math.max(maximum - minimum, 0.000001);
            stream.setStrokingColor(BORDER);
            stream.moveTo(LEFT + 8, bottom); stream.lineTo(LEFT + 8 + chartWidth, bottom); stream.stroke();
            stream.setStrokingColor(BLUE); stream.setLineWidth(1.8f);
            for (int index = 0; index < kpi.points().size(); index++) {
                float x = LEFT + 8 + chartWidth * index / (kpi.points().size() - 1f);
                float pointY = bottom + 7 + (float) ((kpi.points().get(index).value().doubleValue() - minimum) / range) * (chartHeight - 12);
                if (index == 0) stream.moveTo(x, pointY); else stream.lineTo(x, pointY);
            }
            stream.stroke();
            for (int index = 0; index < kpi.points().size(); index++) {
                float x = LEFT + 8 + chartWidth * index / (kpi.points().size() - 1f);
                float pointY = bottom + 7 + (float) ((kpi.points().get(index).value().doubleValue() - minimum) / range) * (chartHeight - 12);
                stream.setNonStrokingColor(BLUE); stream.addRect(x - 1.5f, pointY - 1.5f, 3, 3); stream.fill();
            }
            y = bottom - 6;
        }

        private void summary() throws IOException {
            long reports = data.rows().stream().map(ReportGenerationData.Row::sourceReportId).distinct().count();
            long missing = data.rows().stream().filter(row -> row.confirmedValue() == null).count();
            float gap = 9;
            float cardWidth = (WIDTH - gap * 2) / 3;
            summaryCard(LEFT, cardWidth, "RAPPORTS SOURCES", Long.toString(reports));
            summaryCard(LEFT + cardWidth + gap, cardWidth, "INDICATEURS", Integer.toString(data.rows().size()));
            summaryCard(LEFT + (cardWidth + gap) * 2, cardWidth, "NON RENSEIGNÉS", Long.toString(missing));
            y -= 58;
        }

        private void summaryCard(float x, float width, String label, String value) throws IOException {
            stream.setNonStrokingColor(CREAM); stream.addRect(x, y - 48, width, 48); stream.fill();
            stream.setStrokingColor(BORDER); stream.addRect(x, y - 48, width, 48); stream.stroke();
            drawText(stream, BOLD, 7.5f, MUTED, x + 10, y - 15, label);
            drawText(stream, BOLD, 18, INK, x + 10, y - 38, value);
        }

        private void traceability() throws IOException {
            if (data.rows().isEmpty()) return;
            List<String[]> values = new ArrayList<>();
            Set<String> submitters = new LinkedHashSet<>();
            data.rows().stream().map(ReportGenerationData.Row::submittedBy)
                    .filter(name -> name != null && !name.isBlank()).forEach(submitters::add);
            if (!submitters.isEmpty()) values.add(new String[]{"Soumis par", String.join(", ", submitters)});
            List<String> confirmations = data.rows().stream().map(ReportGenerationData.Row::confirmedAt)
                    .filter(java.util.Objects::nonNull).sorted().map(ReportDocumentText::instant).distinct().toList();
            if (!confirmations.isEmpty()) {
                String confirmed = confirmations.getFirst();
                if (confirmations.size() > 1) {
                    confirmed += " - " + confirmations.getLast();
                }
                values.add(new String[]{"Confirmé le", confirmed});
            }
            values.add(new String[]{"Période", ReportDocumentText.period(data)});
            values.add(new String[]{"Généré le", ReportDocumentText.instant(data.generatedAt())});

            float blockHeight = values.size() * 16 + 12;
            stream.setNonStrokingColor(CREAM); stream.addRect(LEFT, y - blockHeight, WIDTH, blockHeight); stream.fill();
            stream.setStrokingColor(BORDER); stream.addRect(LEFT, y - blockHeight, WIDTH, blockHeight); stream.stroke();
            float lineY = y - 16;
            for (String[] value : values) {
                drawText(stream, BOLD, 7.5f, MUTED, LEFT + 10, lineY, value[0].toUpperCase(java.util.Locale.FRENCH));
                drawText(stream, REGULAR, 8, INK, LEFT + 92, lineY, value[1]);
                lineY -= 16;
            }
            y -= blockHeight;
        }

        private void sectionTitle(String text) throws IOException {
            stream.setNonStrokingColor(MAGENTA); stream.addRect(LEFT, y - 22, WIDTH, 22); stream.fill();
            drawText(stream, BOLD, 9, Color.WHITE, LEFT + 9, y - 15, text);
            y -= 22;
        }

        private void tableHeader() throws IOException {
            float[] widths = {62, 165, 82, 52, 72, 78};
            String[] headers = {"DATE", "INDICATEUR", "VALEUR", "UNITÉ", "STATUT", "RAPPORT SOURCE"};
            float x = LEFT;
            stream.setNonStrokingColor(INK); stream.addRect(LEFT, y - 24, WIDTH, 24); stream.fill();
            for (int i = 0; i < headers.length; i++) {
                drawText(stream, BOLD, 7.5f, Color.WHITE, x + 5, y - 15, headers[i]);
                x += widths[i];
            }
            y -= 24;
        }

        private boolean canFitRow() { return y >= 92; }

        private void row(ReportGenerationData.Row row, boolean shaded) throws IOException {
            float height = 31;
            float[] widths = {62, 165, 82, 52, 72, 78};
            if (shaded) { stream.setNonStrokingColor(CREAM); stream.addRect(LEFT, y - height, WIDTH, height); stream.fill(); }
            stream.setStrokingColor(BORDER); stream.addRect(LEFT, y - height, WIDTH, height); stream.stroke();
            float x = LEFT;
            for (float width : widths) { stream.moveTo(x, y); stream.lineTo(x, y - height); stream.stroke(); x += width; }
            stream.moveTo(RIGHT, y); stream.lineTo(RIGHT, y - height); stream.stroke();

            x = LEFT;
            drawCell(ReportDocumentText.date(row.effectiveDate()), REGULAR, 7.5f, INK, x, widths[0], y); x += widths[0];
            drawCell(row.kpiName(), BOLD, 8, INK, x, widths[1], y); x += widths[1];
            boolean missing = row.confirmedValue() == null;
            String displayedValue = ReportDocumentText.value(row.confirmedValue());
            if (row.secondaryConfirmedValue() != null) {
                displayedValue += " · " + ReportDocumentText.value(row.secondaryConfirmedValue())
                        + (row.secondaryUnit() == null ? "" : row.secondaryUnit());
            }
            drawCell(displayedValue, missing ? ITALIC : BOLD, missing ? 7.2f : 9,
                    missing ? MUTED : INK, x, widths[2], y); x += widths[2];
            drawCell(ReportDocumentText.unit(row.unit()), REGULAR, 7.5f, MUTED, x, widths[3], y); x += widths[3];
            drawCell(ReportDocumentText.CONFIRMED, BOLD, 7.2f, GREEN, x, widths[4], y); x += widths[4];
            drawCell("N°" + row.sourceReportId(), REGULAR, 7.5f, MUTED, x, widths[5], y);
            y -= height;
        }

        private void drawCell(String value, PDFont font, float size, Color color, float x, float width, float top)
                throws IOException {
            List<String> lines = wrap(font, size, safe(value), width - 10, 2);
            float textY = top - 12;
            for (String line : lines) {
                drawText(stream, font, size, color, x + 5, textY, line);
                textY -= size + 2;
            }
        }

        private void emptyMessage() throws IOException {
            stream.setNonStrokingColor(CREAM); stream.addRect(LEFT, y - 54, WIDTH, 54); stream.fill();
            drawText(stream, ITALIC, 9, MUTED, LEFT + 14, y - 31,
                    "Aucune donnée de maintenance confirmée pour cette période.");
            y -= 54;
        }

        private void drawRight(PDFont font, float size, Color color, String text, float textY) throws IOException {
            drawText(stream, font, size, color, RIGHT - textWidth(font, size, safe(text)), textY, text);
        }

        @Override public void close() throws IOException { stream.close(); }
    }

    private static List<String> wrap(PDFont font, float size, String value, float maxWidth, int maxLines)
            throws IOException {
        List<String> lines = new ArrayList<>();
        String remaining = safe(value);
        while (!remaining.isBlank() && lines.size() < maxLines) {
            String line = remaining;
            while (textWidth(font, size, line) > maxWidth && line.length() > 1) {
                int split = line.lastIndexOf(' ');
                line = split > 0 ? line.substring(0, split) : line.substring(0, line.length() - 1);
            }
            if (lines.size() == maxLines - 1 && line.length() < remaining.length()) {
                while (textWidth(font, size, line + "...") > maxWidth && line.length() > 1) line = line.substring(0, line.length() - 1);
                line += "...";
                remaining = "";
            } else {
                remaining = remaining.substring(Math.min(line.length(), remaining.length())).stripLeading();
            }
            lines.add(line);
        }
        if (lines.isEmpty()) lines.add(ReportDocumentText.MISSING);
        return lines;
    }

    private static void drawText(PDPageContentStream stream, PDFont font, float size, Color color, float x, float y,
                                 String text) throws IOException {
        stream.beginText(); stream.setFont(font, size); stream.setNonStrokingColor(color);
        stream.newLineAtOffset(x, y); stream.showText(safe(text)); stream.endText();
    }

    private static float textWidth(PDFont font, float size, String text) throws IOException {
        return font.getStringWidth(safe(text)) / 1000 * size;
    }

    private static String safe(String value) {
        if (value == null || value.isBlank()) return ReportDocumentText.MISSING;
        return value.replace('’', '\'').replace('–', '-').replace('…', '.').replace('\n', ' ').replace('\r', ' ');
    }
}
