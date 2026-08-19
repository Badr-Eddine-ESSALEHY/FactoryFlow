package com.factoryflow.generatedreport.application;

import com.factoryflow.analytics.domain.AnalyticsSnapshot;
import com.factoryflow.generatedreport.domain.GeneratedReportType;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

@Component
public class PdfReportGenerator {

    /*
     * Corporate wordmark colors are intentionally used only inside the
     * Alf Mabrouk wordmark. The report itself has an independent, restrained
     * industrial palette.
     */
    private static final Color LOGO_MAGENTA = new Color(194, 47, 138);
    private static final Color LOGO_GREEN = new Color(91, 154, 47);

    private static final Color NAVY = new Color(23, 34, 51);
    private static final Color NAVY_SOFT = new Color(46, 59, 76);
    private static final Color TEAL = new Color(63, 119, 118);
    private static final Color TEAL_DARK = new Color(46, 91, 91);
    private static final Color INK = new Color(35, 43, 54);
    private static final Color MUTED = new Color(103, 112, 124);
    private static final Color BORDER = new Color(219, 224, 230);
    private static final Color SOFT = new Color(247, 248, 250);
    private static final Color SOFT_ALT = new Color(242, 245, 247);
    private static final Color WHITE = Color.WHITE;

    private static final PDType1Font REGULAR =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDType1Font ITALIC =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float LEFT = 42f;
    private static final float RIGHT = PAGE_WIDTH - 42f;
    private static final float WIDTH = RIGHT - LEFT;
    private static final float FOOTER_LINE_Y = 43f;

    private static final DateTimeFormatter SHORT_DATE =
            DateTimeFormatter.ofPattern("dd/MM", Locale.FRENCH);

    public byte[] generate(ReportGenerationData data) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            applyMetadata(document, data);

            PDImageXObject wordmark = createCorporateWordmark(document);

            PageWriter summary = createPage(document, data, wordmark);
            summary.renderSummaryPage();
            summary.close();

            if (!data.rows().isEmpty()) {
                PageWriter detail = createPage(document, data, wordmark);
                detail.renderDetailHeader();

                int index = 0;
                for (ReportGenerationData.Row row : data.rows()) {
                    float rowHeight = detail.requiredRowHeight(row);

                    if (!detail.canFit(rowHeight)) {
                        detail.close();
                        detail = createPage(document, data, wordmark);
                        detail.renderDetailHeader();
                    }

                    detail.renderRow(row, index++ % 2 == 1, rowHeight);
                }

                detail.close();
            }

            addFooters(document, data);
            document.save(output);
            return output.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException("PDF generation failed", exception);
        }
    }

    private void applyMetadata(
            PDDocument document,
            ReportGenerationData data
    ) {
        PDDocumentInformation info = new PDDocumentInformation();
        info.setTitle(ReportDocumentText.title(data.type()));
        info.setSubject(
                "Indicateurs de maintenance confirmés · "
                        + ReportDocumentText.period(data)
        );
        info.setAuthor(ReportDocumentText.BRAND);
        info.setCreator(ReportDocumentText.PRODUCT);
        info.setKeywords(
                "maintenance, KPI, FactoryFlow, Alf Mabrouk, rapport confirmé"
        );
        document.setDocumentInformation(info);
    }

    private PageWriter createPage(
            PDDocument document,
            ReportGenerationData data,
            PDImageXObject wordmark
    ) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        return new PageWriter(
                new PDPageContentStream(document, page),
                data,
                wordmark
        );
    }

    /**
     * Builds the Alf Mabrouk wordmark at runtime on a transparent canvas.
     * The PDF therefore does not depend on a baked logo background.
     */
    private PDImageXObject createCorporateWordmark(
            PDDocument document
    ) throws IOException {
        int width = 650;
        int height = 260;

        BufferedImage image =
                new BufferedImage(
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

            Font arabic =
                    new Font("Arial", Font.BOLD, 72);

            TextLayout arabicLayout =
                    new TextLayout(
                            "علف مبروك",
                            arabic,
                            graphics.getFontRenderContext()
                    );

            float arabicX =
                    (width - arabicLayout.getAdvance()) / 2f;

            graphics.setColor(LOGO_MAGENTA);
            arabicLayout.draw(
                    graphics,
                    arabicX,
                    78f
            );

            Font latin =
                    new Font(
                            "Arial",
                            Font.BOLD,
                            38
                    );

            graphics.setFont(latin);
            graphics.setColor(WHITE);

            drawCenteredLetterSpaced(
                    graphics,
                    "ALF MABROUK",
                    width / 2f,
                    146f,
                    3.7f
            );

            Font subtitle =
                    new Font(
                            "Arial",
                            Font.PLAIN,
                            21
                    );

            graphics.setFont(subtitle);
            graphics.setColor(LOGO_GREEN);

            drawCenteredLetterSpaced(
                    graphics,
                    "NUTRITION ANIMALE",
                    width / 2f,
                    202f,
                    5.8f
            );

            graphics.setColor(LOGO_MAGENTA);
            graphics.fillRect(
                    width / 2 - 48,
                    232,
                    86,
                    4
            );

        } finally {
            graphics.dispose();
        }

        return LosslessFactory.createFromImage(
                document,
                image
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

    private void addFooters(
            PDDocument document,
            ReportGenerationData data
    ) throws IOException {
        int total = document.getNumberOfPages();

        for (int index = 0; index < total; index++) {
            PDPage page = document.getPage(index);

            try (PDPageContentStream stream =
                         new PDPageContentStream(
                                 document,
                                 page,
                                 PDPageContentStream.AppendMode.APPEND,
                                 true,
                                 true
                         )) {

                stream.setStrokingColor(BORDER);
                stream.setLineWidth(0.7f);
                stream.moveTo(LEFT, FOOTER_LINE_Y);
                stream.lineTo(RIGHT, FOOTER_LINE_Y);
                stream.stroke();

                drawText(
                        stream,
                        REGULAR,
                        7.4f,
                        MUTED,
                        LEFT,
                        27,
                        "Alf Mabrouk · FactoryFlow · "
                                + ReportDocumentText.instant(
                                data.generatedAt()
                        )
                );

                String pageLabel =
                        "Page " + (index + 1) + " / " + total;

                drawText(
                        stream,
                        BOLD,
                        7.4f,
                        NAVY,
                        RIGHT
                                - textWidth(
                                BOLD,
                                7.4f,
                                pageLabel
                        ),
                        27,
                        pageLabel
                );
            }
        }
    }

    private static final class PageWriter
            implements AutoCloseable {

        private final PDPageContentStream stream;
        private final ReportGenerationData data;
        private final PDImageXObject wordmark;

        private float y;

        private PageWriter(
                PDPageContentStream stream,
                ReportGenerationData data,
                PDImageXObject wordmark
        ) {
            this.stream = stream;
            this.data = data;
            this.wordmark = wordmark;
            this.y = PAGE_HEIGHT;
        }

        private void renderSummaryPage()
                throws IOException {

            renderHeroHeader();

            drawText(
                    stream,
                    REGULAR,
                    9.5f,
                    INK,
                    LEFT,
                    y,
                    "Vue consolidée des indicateurs de maintenance confirmés "
                            + "pour la période sélectionnée."
            );

            y -= 28;

            renderSummaryCards();

            y -= 18;

            renderDataQuality();

            y -= 20;

            renderExecutiveAnalysis();

            y -= 18;
            renderTraceability();

            AnalyticsSnapshot.KpiAnalytics chartKpi =
                    bestChartCandidate();

            if (chartKpi != null
                    && data.type() != GeneratedReportType.DAILY) {

                y -= 13;
                renderTrendChart(chartKpi);
            }
        }

        private void renderDetailHeader()
                throws IOException {

            renderCompactHeader();

            sectionHeading(
                    "DÉTAIL DES INDICATEURS CONFIRMÉS"
            );

            drawText(
                    stream,
                    REGULAR,
                    8.2f,
                    MUTED,
                    LEFT,
                    y - 13,
                    "Seules les valeurs confirmées sont présentées. "
                            + "Une valeur absente reste « Non renseigné »."
            );

            y -= 31;

            renderTableHeader();
        }

        private void renderHeroHeader()
                throws IOException {

            float headerHeight = 138f;
            float bottom =
                    PAGE_HEIGHT - headerHeight;

            stream.setNonStrokingColor(NAVY);
            stream.addRect(
                    0,
                    bottom,
                    PAGE_WIDTH,
                    headerHeight
            );
            stream.fill();

            float logoWidth = 184f;
            float logoHeight =
                    logoWidth
                            * wordmark.getHeight()
                            / wordmark.getWidth();

            float logoY =
                    PAGE_HEIGHT
                            - 22
                            - logoHeight;

            stream.drawImage(
                    wordmark,
                    LEFT,
                    logoY,
                    logoWidth,
                    logoHeight
            );

            String title =
                    ReportDocumentText.title(
                                    data.type()
                            )
                            .toUpperCase(
                                    Locale.FRENCH
                            );

            drawRight(
                    BOLD,
                    15.5f,
                    WHITE,
                    title,
                    PAGE_HEIGHT - 46
            );

            drawRight(
                    REGULAR,
                    9.3f,
                    new Color(209, 218, 230),
                    ReportDocumentText.period(data),
                    PAGE_HEIGHT - 66
            );

            drawStatusPill(
                    RIGHT - 128,
                    PAGE_HEIGHT - 100,
                    128,
                    22,
                    "DONNÉES CONFIRMÉES"
            );

            stream.setNonStrokingColor(TEAL);
            stream.addRect(
                    0,
                    bottom,
                    PAGE_WIDTH,
                    2
            );
            stream.fill();

            y = bottom - 24;
        }

        private void renderCompactHeader()
                throws IOException {

            float headerHeight = 86f;
            float bottom =
                    PAGE_HEIGHT - headerHeight;

            stream.setNonStrokingColor(NAVY);
            stream.addRect(
                    0,
                    bottom,
                    PAGE_WIDTH,
                    headerHeight
            );
            stream.fill();

            float logoWidth = 132f;
            float logoHeight =
                    logoWidth
                            * wordmark.getHeight()
                            / wordmark.getWidth();

            stream.drawImage(
                    wordmark,
                    LEFT,
                    PAGE_HEIGHT - 18 - logoHeight,
                    logoWidth,
                    logoHeight
            );

            drawRight(
                    BOLD,
                    11.5f,
                    WHITE,
                    ReportDocumentText.title(
                                    data.type()
                            )
                            .toUpperCase(
                                    Locale.FRENCH
                            ),
                    PAGE_HEIGHT - 32
            );

            drawRight(
                    REGULAR,
                    8f,
                    new Color(209, 218, 230),
                    ReportDocumentText.period(data),
                    PAGE_HEIGHT - 49
            );

            stream.setNonStrokingColor(TEAL);
            stream.addRect(
                    0,
                    bottom,
                    PAGE_WIDTH,
                    2
            );
            stream.fill();

            y = bottom - 22;
        }

        private void drawStatusPill(
                float x,
                float top,
                float width,
                float height,
                String label
        ) throws IOException {

            float bottom = top - height;

            fillRoundedRect(
                    stream,
                    x,
                    bottom,
                    width,
                    height,
                    9,
                    NAVY_SOFT
            );

            float size = 7.2f;

            drawText(
                    stream,
                    BOLD,
                    size,
                    WHITE,
                    x
                            + (width
                            - textWidth(
                            BOLD,
                            size,
                            label
                    )) / 2f,
                    bottom + 7.5f,
                    label
            );
        }

        private void renderSummaryCards()
                throws IOException {

            long sourceReports =
                    data.rows()
                            .stream()
                            .map(
                                    ReportGenerationData.Row
                                            ::sourceReportId
                            )
                            .distinct()
                            .count();

            long missing =
                    data.rows()
                            .stream()
                            .filter(
                                    row ->
                                            row.confirmedValue()
                                                    == null
                            )
                            .count();

            String completeness =
                    data.analytics()
                                    .completenessRate()
                            == null
                            ? "—"
                            : data.analytics()
                            .completenessRate()
                            .stripTrailingZeros()
                            .toPlainString()
                            + " %";

            float gap = 8f;
            float cardWidth =
                    (WIDTH - gap * 3) / 4f;
            float height = 58f;

            renderCard(
                    LEFT,
                    cardWidth,
                    height,
                    "RAPPORTS SOURCES",
                    Long.toString(sourceReports),
                    TEAL
            );

            renderCard(
                    LEFT + cardWidth + gap,
                    cardWidth,
                    height,
                    "INDICATEURS",
                    Integer.toString(
                            data.rows().size()
                    ),
                    TEAL
            );

            renderCard(
                    LEFT
                            + (cardWidth + gap) * 2,
                    cardWidth,
                    height,
                    "COMPLÉTUDE",
                    completeness,
                    TEAL
            );

            renderCard(
                    LEFT
                            + (cardWidth + gap) * 3,
                    cardWidth,
                    height,
                    "NON RENSEIGNÉS",
                    Long.toString(missing),
                    TEAL
            );

            y -= height;
        }

        private void renderCard(
                float x,
                float width,
                float height,
                String label,
                String value,
                Color accent
        ) throws IOException {

            float bottom = y - height;

            fillRoundedRect(
                    stream,
                    x,
                    bottom,
                    width,
                    height,
                    7,
                    SOFT
            );

            strokeRoundedRect(
                    stream,
                    x,
                    bottom,
                    width,
                    height,
                    7,
                    BORDER
            );

            stream.setNonStrokingColor(accent);
            stream.addRect(
                    x + 9,
                    y - 32,
                    3,
                    20
            );
            stream.fill();

            drawText(
                    stream,
                    BOLD,
                    6.8f,
                    MUTED,
                    x + 19,
                    y - 20,
                    label
            );

            drawText(
                    stream,
                    BOLD,
                    15.2f,
                    NAVY,
                    x + 19,
                    y - 43,
                    value
            );
        }

        private void renderDataQuality()
                throws IOException {

            sectionHeading(
                    "QUALITÉ DES DONNÉES"
            );

            float panelHeight = 66f;
            float bottom =
                    y - panelHeight;

            fillRoundedRect(
                    stream,
                    LEFT,
                    bottom,
                    WIDTH,
                    panelHeight,
                    7,
                    SOFT_ALT
            );

            BigDecimalWrapper rate =
                    completeness();

            drawText(
                    stream,
                    BOLD,
                    8.3f,
                    NAVY,
                    LEFT + 12,
                    y - 18,
                    "Complétude des données"
            );

            drawText(
                    stream,
                    BOLD,
                    17f,
                    NAVY,
                    RIGHT - 70,
                    y - 19,
                    rate.label()
            );

            float barX = LEFT + 12;
            float barY = y - 38;
            float barWidth = WIDTH - 24;
            float barHeight = 7;

            fillRoundedRect(
                    stream,
                    barX,
                    barY,
                    barWidth,
                    barHeight,
                    3.5f,
                    BORDER
            );

            if (rate.percent() != null
                    && rate.percent() > 0) {

                fillRoundedRect(
                        stream,
                        barX,
                        barY,
                        (float) (
                                barWidth
                                        * Math.min(
                                        100d,
                                        rate.percent()
                                )
                                        / 100d
                        ),
                        barHeight,
                        3.5f,
                        TEAL
                );
            }

            String qualityText =
                    data.analytics()
                            .measurementCount()
                            + " mesures confirmées · "
                            + data.analytics()
                            .missingValueCount()
                            + " non renseignées";

            drawText(
                    stream,
                    REGULAR,
                    7.8f,
                    MUTED,
                    LEFT + 12,
                    bottom + 9,
                    qualityText
            );

            y = bottom;
        }

        private BigDecimalWrapper completeness() {

            if (data.analytics()
                            .completenessRate()
                    == null) {
                return new BigDecimalWrapper(
                        null,
                        "—"
                );
            }

            return new BigDecimalWrapper(
                    data.analytics()
                            .completenessRate()
                            .doubleValue(),
                    data.analytics()
                            .completenessRate()
                            .stripTrailingZeros()
                            .toPlainString()
                            + " %"
            );
        }

        private void renderExecutiveAnalysis()
                throws IOException {

            sectionHeading(
                    "SYNTHÈSE EXÉCUTIVE"
            );

            if (data.analytics()
                    .kpis()
                    .isEmpty()) {

                renderSoftMessage(
                        "Aucune donnée de maintenance confirmée "
                                + "pour cette période."
                );
                return;
            }

            float[] widths =
                    {
                            152f,
                            72f,
                            72f,
                            86f,
                            72f,
                            57f
                    };

            String[] headers =
                    {
                            "INDICATEUR",
                            "DERNIÈRE",
                            "MOYENNE",
                            "MIN / MAX",
                            "TENDANCE",
                            "COMPL."
                    };

            renderMiniTableHeader(
                    widths,
                    headers
            );

            List<AnalyticsSnapshot.KpiAnalytics> kpis =
                    data.analytics()
                            .kpis()
                            .stream()
                            .limit(4)
                            .toList();

            int index = 0;

            for (AnalyticsSnapshot.KpiAnalytics kpi
                    : kpis) {

                renderExecutiveRow(
                        kpi,
                        widths,
                        index++ % 2 == 1
                );
            }

            int remaining =
                    data.analytics()
                            .kpis()
                            .size()
                            - kpis.size();

            if (remaining > 0) {
                drawText(
                        stream,
                        ITALIC,
                        7.5f,
                        MUTED,
                        LEFT,
                        y - 12,
                        "+ "
                                + remaining
                                + " autre"
                                + (remaining > 1
                                ? "s"
                                : "")
                                + " indicateur"
                                + (remaining > 1
                                ? "s"
                                : "")
                                + " disponible"
                                + (remaining > 1
                                ? "s"
                                : "")
                                + " dans le détail."
                );

                y -= 18;
            }
        }

        private void renderMiniTableHeader(
                float[] widths,
                String[] headers
        ) throws IOException {

            float height = 22f;
            float x = LEFT;

            stream.setNonStrokingColor(NAVY_SOFT);
            stream.addRect(
                    LEFT,
                    y - height,
                    WIDTH,
                    height
            );
            stream.fill();

            for (int index = 0;
                 index < headers.length;
                 index++) {

                drawText(
                        stream,
                        BOLD,
                        6.6f,
                        WHITE,
                        x + 6,
                        y - 14,
                        headers[index]
                );

                x += widths[index];
            }

            y -= height;
        }

        private void renderExecutiveRow(
                AnalyticsSnapshot.KpiAnalytics kpi,
                float[] widths,
                boolean alternate
        ) throws IOException {

            float height = 27f;

            if (alternate) {
                stream.setNonStrokingColor(SOFT);
                stream.addRect(
                        LEFT,
                        y - height,
                        WIDTH,
                        height
                );
                stream.fill();
            }

            stream.setStrokingColor(BORDER);
            stream.setLineWidth(0.4f);
            stream.moveTo(
                    LEFT,
                    y - height
            );
            stream.lineTo(
                    RIGHT,
                    y - height
            );
            stream.stroke();

            float x = LEFT;

            drawCell(
                    kpi.displayName(),
                    BOLD,
                    7.4f,
                    INK,
                    x,
                    widths[0],
                    y,
                    1
            );

            x += widths[0];

            drawCell(
                    valueWithUnit(
                            kpi.latest(),
                            kpi.unit()
                    ),
                    REGULAR,
                    7.3f,
                    INK,
                    x,
                    widths[1],
                    y,
                    1
            );

            x += widths[1];

            drawCell(
                    valueWithUnit(
                            kpi.mean(),
                            kpi.unit()
                    ),
                    REGULAR,
                    7.3f,
                    INK,
                    x,
                    widths[2],
                    y,
                    1
            );

            x += widths[2];

            String minMax =
                    kpi.minimum() == null
                            || kpi.maximum() == null
                            ? "—"
                            : ReportDocumentText.value(
                            kpi.minimum()
                    )
                            + " / "
                            + ReportDocumentText.value(
                            kpi.maximum()
                    );

            drawCell(
                    minMax,
                    REGULAR,
                    7.2f,
                    MUTED,
                    x,
                    widths[3],
                    y,
                    1
            );

            x += widths[3];

            drawCell(
                    trendLabel(
                            kpi.trend()
                    ),
                    BOLD,
                    7f,
                    trendColor(
                            kpi.trend()
                    ),
                    x,
                    widths[4],
                    y,
                    1
            );

            x += widths[4];

            String completeness =
                    kpi.completenessRate()
                            == null
                            ? "—"
                            : kpi.completenessRate()
                            .stripTrailingZeros()
                            .toPlainString()
                            + "%";

            drawCell(
                    completeness,
                    BOLD,
                    7.2f,
                    NAVY,
                    x,
                    widths[5],
                    y,
                    1
            );

            y -= height;
        }

        private AnalyticsSnapshot.KpiAnalytics
        bestChartCandidate() {

            return data.analytics()
                    .kpis()
                    .stream()
                    .filter(
                            kpi ->
                                    kpi.points()
                                            .size()
                                            >= 2
                    )
                    .max(
                            Comparator.comparingInt(
                                    kpi ->
                                            kpi.points()
                                                    .size()
                            )
                    )
                    .orElse(null);
        }

        private void renderTrendChart(
                AnalyticsSnapshot.KpiAnalytics kpi
        ) throws IOException {

            sectionHeading(
                    "ÉVOLUTION — "
                            + kpi.displayName()
                            .toUpperCase(
                                    Locale.FRENCH
                            )
            );

            float cardHeight = 100f;
            float bottom = y - cardHeight;

            fillRoundedRect(
                    stream,
                    LEFT,
                    bottom,
                    WIDTH,
                    cardHeight,
                    7,
                    SOFT
            );

            strokeRoundedRect(
                    stream,
                    LEFT,
                    bottom,
                    WIDTH,
                    cardHeight,
                    7,
                    BORDER
            );

            float plotLeft = LEFT + 42;
            float plotRight = RIGHT - 16;
            float plotBottom = bottom + 24;
            float plotTop = y - 20;
            float plotWidth =
                    plotRight - plotLeft;
            float plotHeight =
                    plotTop - plotBottom;

            double minimum =
                    kpi.points()
                            .stream()
                            .mapToDouble(
                                    point ->
                                            point.value()
                                                    .doubleValue()
                            )
                            .min()
                            .orElse(0d);

            double maximum =
                    kpi.points()
                            .stream()
                            .mapToDouble(
                                    point ->
                                            point.value()
                                                    .doubleValue()
                            )
                            .max()
                            .orElse(minimum);

            double range = maximum - minimum;

            if (Math.abs(range) < 0.000001d) {
                range = 1d;
                minimum -= 0.5d;
                maximum += 0.5d;
            }

            stream.setStrokingColor(BORDER);
            stream.setLineWidth(0.6f);

            stream.moveTo(
                    plotLeft,
                    plotBottom
            );
            stream.lineTo(
                    plotRight,
                    plotBottom
            );
            stream.stroke();

            stream.moveTo(
                    plotLeft,
                    plotBottom
            );
            stream.lineTo(
                    plotLeft,
                    plotTop
            );
            stream.stroke();

            drawText(
                    stream,
                    REGULAR,
                    6.7f,
                    MUTED,
                    LEFT + 8,
                    plotTop - 2,
                    ReportDocumentText.value(
                            java.math.BigDecimal
                                    .valueOf(maximum)
                    )
            );

            drawText(
                    stream,
                    REGULAR,
                    6.7f,
                    MUTED,
                    LEFT + 8,
                    plotBottom - 2,
                    ReportDocumentText.value(
                            java.math.BigDecimal
                                    .valueOf(minimum)
                    )
            );

            stream.setStrokingColor(TEAL);
            stream.setLineWidth(1.8f);

            for (int index = 0;
                 index < kpi.points().size();
                 index++) {

                var point =
                        kpi.points().get(index);

                float x =
                        plotLeft
                                + plotWidth
                                * index
                                / (kpi.points()
                                .size()
                                - 1f);

                float pointY =
                        plotBottom
                                + (float) (
                                (point.value()
                                        .doubleValue()
                                        - minimum)
                                        / range
                        )
                                * plotHeight;

                if (index == 0) {
                    stream.moveTo(
                            x,
                            pointY
                    );
                } else {
                    stream.lineTo(
                            x,
                            pointY
                    );
                }
            }

            stream.stroke();

            for (int index = 0;
                 index < kpi.points().size();
                 index++) {

                var point =
                        kpi.points().get(index);

                float x =
                        plotLeft
                                + plotWidth
                                * index
                                / (kpi.points()
                                .size()
                                - 1f);

                float pointY =
                        plotBottom
                                + (float) (
                                (point.value()
                                        .doubleValue()
                                        - minimum)
                                        / range
                        )
                                * plotHeight;

                stream.setNonStrokingColor(TEAL);
                stream.addRect(
                        x - 1.8f,
                        pointY - 1.8f,
                        3.6f,
                        3.6f
                );
                stream.fill();
            }

            var first =
                    kpi.points().getFirst();

            var last =
                    kpi.points().getLast();

            drawText(
                    stream,
                    REGULAR,
                    6.6f,
                    MUTED,
                    plotLeft,
                    bottom + 9,
                    SHORT_DATE.format(
                            first.effectiveDate()
                    )
            );

            String lastDate =
                    SHORT_DATE.format(
                            last.effectiveDate()
                    );

            drawText(
                    stream,
                    REGULAR,
                    6.6f,
                    MUTED,
                    plotRight
                            - textWidth(
                            REGULAR,
                            6.6f,
                            lastDate
                    ),
                    bottom + 9,
                    lastDate
            );

            y = bottom;
        }

        private void renderTraceability()
                throws IOException {

            sectionHeading(
                    "TRAÇABILITÉ"
            );

            Set<String> submitters =
                    new LinkedHashSet<>();

            data.rows()
                    .stream()
                    .map(
                            ReportGenerationData.Row
                                    ::submittedBy
                    )
                    .filter(
                            value ->
                                    value != null
                                            && !value.isBlank()
                    )
                    .forEach(
                            submitters::add
                    );

            List<String> confirmations =
                    data.rows()
                            .stream()
                            .map(
                                    ReportGenerationData.Row
                                            ::confirmedAt
                            )
                            .filter(
                                    java.util.Objects
                                            ::nonNull
                            )
                            .sorted()
                            .map(
                                    ReportDocumentText
                                            ::instant
                            )
                            .distinct()
                            .toList();

            String confirmationText = "—";

            if (!confirmations.isEmpty()) {
                confirmationText =
                        confirmations.getFirst();

                if (confirmations.size() > 1) {
                    confirmationText +=
                            " - "
                                    + confirmations
                                    .getLast();
                }
            }

            String submitterText =
                    submitters.isEmpty()
                            ? "—"
                            : String.join(
                            ", ",
                            submitters
                    );

            float panelHeight = 78f;
            float bottom =
                    y - panelHeight;

            fillRoundedRect(
                    stream,
                    LEFT,
                    bottom,
                    WIDTH,
                    panelHeight,
                    7,
                    SOFT_ALT
            );

            float columnWidth =
                    (WIDTH - 28) / 2f;

            metadataPair(
                    LEFT + 12,
                    y - 17,
                    columnWidth,
                    "SOUMIS PAR",
                    submitterText
            );

            metadataPair(
                    LEFT + 16 + columnWidth,
                    y - 17,
                    columnWidth,
                    "CONFIRMÉ LE",
                    confirmationText
            );

            metadataPair(
                    LEFT + 12,
                    y - 49,
                    columnWidth,
                    "PÉRIODE",
                    ReportDocumentText.period(data)
            );

            metadataPair(
                    LEFT + 16 + columnWidth,
                    y - 49,
                    columnWidth,
                    "GÉNÉRÉ LE",
                    ReportDocumentText.instant(
                            data.generatedAt()
                    )
            );

            y = bottom;
        }

        private void metadataPair(
                float x,
                float top,
                float width,
                String label,
                String value
        ) throws IOException {

            drawText(
                    stream,
                    BOLD,
                    6.7f,
                    TEAL_DARK,
                    x,
                    top,
                    label
            );

            drawCell(
                    value,
                    REGULAR,
                    8f,
                    INK,
                    x - 5,
                    width,
                    top - 4,
                    1
            );
        }

        private void sectionHeading(
                String title
        ) throws IOException {

            stream.setNonStrokingColor(TEAL);
            stream.addRect(
                    LEFT,
                    y - 16,
                    3,
                    16
            );
            stream.fill();

            drawText(
                    stream,
                    BOLD,
                    9.4f,
                    NAVY,
                    LEFT + 11,
                    y - 13,
                    title
            );

            float titleWidth =
                    textWidth(
                            BOLD,
                            9.4f,
                            title
                    );

            stream.setStrokingColor(BORDER);
            stream.setLineWidth(0.6f);
            stream.moveTo(
                    LEFT
                            + 20
                            + titleWidth,
                    y - 10
            );
            stream.lineTo(
                    RIGHT,
                    y - 10
            );
            stream.stroke();

            y -= 27;
        }

        private void renderTableHeader()
                throws IOException {

            float[] widths =
                    detailWidths();

            String[] headers =
                    {
                            "DATE",
                            "INDICATEUR",
                            "VALEUR",
                            "UNITÉ",
                            "STATUT",
                            "RAPPORT SOURCE"
                    };

            float height = 25f;
            float x = LEFT;

            stream.setNonStrokingColor(NAVY);
            stream.addRect(
                    LEFT,
                    y - height,
                    WIDTH,
                    height
            );
            stream.fill();

            for (int index = 0;
                 index < headers.length;
                 index++) {

                drawText(
                        stream,
                        BOLD,
                        6.9f,
                        WHITE,
                        x + 5,
                        y - 16,
                        headers[index]
                );

                x += widths[index];
            }

            y -= height;
        }

        private float requiredRowHeight(
                ReportGenerationData.Row row
        ) throws IOException {

            float[] widths =
                    detailWidths();

            int nameLines =
                    wrap(
                            BOLD,
                            8f,
                            safe(row.kpiName()),
                            widths[1] - 10,
                            2
                    )
                            .size();

            int valueLines =
                    wrap(
                            BOLD,
                            8.5f,
                            displayValue(row),
                            widths[2] - 10,
                            2
                    )
                            .size();

            int lines =
                    Math.max(
                            nameLines,
                            valueLines
                    );

            return Math.max(
                    32f,
                    18f + lines * 9f
            );
        }

        private boolean canFit(
                float rowHeight
        ) {
            return y - rowHeight >= 68f;
        }

        private void renderRow(
                ReportGenerationData.Row row,
                boolean alternate,
                float height
        ) throws IOException {

            float[] widths =
                    detailWidths();

            boolean missing =
                    row.confirmedValue() == null;

            Color background =
                    missing
                            ? SOFT_ALT
                            : alternate
                            ? SOFT
                            : WHITE;

            stream.setNonStrokingColor(background);
            stream.addRect(
                    LEFT,
                    y - height,
                    WIDTH,
                    height
            );
            stream.fill();

            stream.setStrokingColor(BORDER);
            stream.setLineWidth(0.45f);
            stream.addRect(
                    LEFT,
                    y - height,
                    WIDTH,
                    height
            );
            stream.stroke();

            float x = LEFT;

            for (float width : widths) {
                stream.moveTo(
                        x,
                        y
                );
                stream.lineTo(
                        x,
                        y - height
                );
                stream.stroke();
                x += width;
            }

            stream.moveTo(
                    RIGHT,
                    y
            );
            stream.lineTo(
                    RIGHT,
                    y - height
            );
            stream.stroke();

            x = LEFT;

            drawCell(
                    ReportDocumentText.date(
                            row.effectiveDate()
                    ),
                    REGULAR,
                    7.3f,
                    MUTED,
                    x,
                    widths[0],
                    y,
                    2
            );

            x += widths[0];

            drawCell(
                    row.kpiName(),
                    BOLD,
                    8f,
                    INK,
                    x,
                    widths[1],
                    y,
                    2
            );

            x += widths[1];

            drawCell(
                    displayValue(row),
                    missing
                            ? ITALIC
                            : BOLD,
                    missing
                            ? 7.3f
                            : 8.5f,
                    missing
                            ? MUTED
                            : NAVY,
                    x,
                    widths[2],
                    y,
                    2
            );

            x += widths[2];

            drawCell(
                    ReportDocumentText.unit(
                            row.unit()
                    ),
                    REGULAR,
                    7.3f,
                    MUTED,
                    x,
                    widths[3],
                    y,
                    1
            );

            x += widths[3];

            drawCell(
                    ReportDocumentText.CONFIRMED,
                    BOLD,
                    6.9f,
                    TEAL_DARK,
                    x,
                    widths[4],
                    y,
                    1
            );

            x += widths[4];

            drawCell(
                    "N°"
                            + row.sourceReportId(),
                    REGULAR,
                    7.2f,
                    MUTED,
                    x,
                    widths[5],
                    y,
                    1
            );

            y -= height;
        }

        private String displayValue(
                ReportGenerationData.Row row
        ) {
            String primary =
                    ReportDocumentText.value(
                            row.confirmedValue()
                    );

            if (row.secondaryConfirmedValue()
                    == null) {
                return primary;
            }

            return primary
                    + " · "
                    + ReportDocumentText.value(
                    row.secondaryConfirmedValue()
            )
                    + (row.secondaryUnit()
                    == null
                    ? ""
                    : row.secondaryUnit());
        }

        private float[] detailWidths() {
            return new float[]{
                    58f,
                    167f,
                    101f,
                    49f,
                    68f,
                    WIDTH
                            - 58f
                            - 167f
                            - 101f
                            - 49f
                            - 68f
            };
        }

        private void renderSoftMessage(
                String message
        ) throws IOException {

            float height = 48f;

            fillRoundedRect(
                    stream,
                    LEFT,
                    y - height,
                    WIDTH,
                    height,
                    7,
                    SOFT
            );

            drawText(
                    stream,
                    ITALIC,
                    8.4f,
                    MUTED,
                    LEFT + 12,
                    y - 28,
                    message
            );

            y -= height;
        }

        private void drawCell(
                String value,
                PDFont font,
                float size,
                Color color,
                float x,
                float width,
                float top,
                int maxLines
        ) throws IOException {

            List<String> lines =
                    wrap(
                            font,
                            size,
                            safe(value),
                            width - 10,
                            maxLines
                    );

            float textY =
                    top - 12;

            for (String line : lines) {
                drawText(
                        stream,
                        font,
                        size,
                        color,
                        x + 5,
                        textY,
                        line
                );

                textY -= size + 2;
            }
        }

        private void drawRight(
                PDFont font,
                float size,
                Color color,
                String text,
                float textY
        ) throws IOException {

            drawText(
                    stream,
                    font,
                    size,
                    color,
                    RIGHT
                            - textWidth(
                            font,
                            size,
                            safe(text)
                    ),
                    textY,
                    text
            );
        }

        @Override
        public void close()
                throws IOException {
            stream.close();
        }

        private record BigDecimalWrapper(
                Double percent,
                String label
        ) {
        }
    }

    private static String valueWithUnit(
            java.math.BigDecimal value,
            String unit
    ) {
        if (value == null) {
            return ReportDocumentText.MISSING;
        }

        String formatted =
                ReportDocumentText.value(value);

        if (unit == null
                || unit.isBlank()) {
            return formatted;
        }

        return formatted
                + " "
                + unit;
    }

    private static String trendLabel(
            com.factoryflow.analytics.domain.TrendDirection trend
    ) {
        return switch (trend) {
            case INCREASING -> "Hausse";
            case DECREASING -> "Baisse";
            case STABLE -> "Stable";
            case INSUFFICIENT_DATA ->
                    "Données insuffisantes";
        };
    }

    private static Color trendColor(
            com.factoryflow.analytics.domain.TrendDirection trend
    ) {
        return trend == com.factoryflow.analytics.domain.TrendDirection.INSUFFICIENT_DATA
                ? MUTED
                : NAVY;
    }

    private static List<String> wrap(
            PDFont font,
            float size,
            String value,
            float maxWidth,
            int maxLines
    ) throws IOException {

        List<String> lines =
                new ArrayList<>();

        String remaining = safe(value);

        while (!remaining.isBlank()
                && lines.size() < maxLines) {

            String line = remaining;

            while (textWidth(
                    font,
                    size,
                    line
            ) > maxWidth
                    && line.length() > 1) {

                int split =
                        line.lastIndexOf(' ');

                if (split > 0) {
                    line =
                            line.substring(
                                    0,
                                    split
                            );
                } else {
                    line =
                            line.substring(
                                    0,
                                    line.length() - 1
                            );
                }
            }

            if (lines.size()
                    == maxLines - 1
                    && line.length()
                    < remaining.length()) {

                while (textWidth(
                        font,
                        size,
                        line + "..."
                ) > maxWidth
                        && line.length() > 1) {

                    line =
                            line.substring(
                                    0,
                                    line.length() - 1
                            );
                }

                line += "...";
                remaining = "";

            } else {
                remaining =
                        remaining.substring(
                                        Math.min(
                                                line.length(),
                                                remaining.length()
                                        )
                                )
                                .stripLeading();
            }

            lines.add(line);
        }

        if (lines.isEmpty()) {
            lines.add(
                    ReportDocumentText.MISSING
            );
        }

        return lines;
    }

    private static void drawText(
            PDPageContentStream stream,
            PDFont font,
            float size,
            Color color,
            float x,
            float y,
            String text
    ) throws IOException {

        stream.beginText();
        stream.setFont(font, size);
        stream.setNonStrokingColor(color);
        stream.newLineAtOffset(x, y);
        stream.showText(safe(text));
        stream.endText();
    }

    private static float textWidth(
            PDFont font,
            float size,
            String text
    ) throws IOException {

        return font.getStringWidth(
                        safe(text)
                )
                / 1000f
                * size;
    }

    private static String safe(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            return ReportDocumentText.MISSING;
        }

        return value
                .replace('\u00A0', ' ')
                .replace('\u202F', ' ')
                .replace('’', '\'')
                .replace('–', '-')
                .replace('…', '.')
                .replace('\n', ' ')
                .replace('\r', ' ');
    }

    private static void fillRoundedRect(
            PDPageContentStream stream,
            float x,
            float y,
            float width,
            float height,
            float radius,
            Color color
    ) throws IOException {

        stream.setNonStrokingColor(color);
        roundedRectPath(
                stream,
                x,
                y,
                width,
                height,
                radius
        );
        stream.fill();
    }

    private static void strokeRoundedRect(
            PDPageContentStream stream,
            float x,
            float y,
            float width,
            float height,
            float radius,
            Color color
    ) throws IOException {

        stream.setStrokingColor(color);
        stream.setLineWidth(0.7f);

        roundedRectPath(
                stream,
                x,
                y,
                width,
                height,
                radius
        );

        stream.stroke();
    }

    private static void roundedRectPath(
            PDPageContentStream stream,
            float x,
            float y,
            float width,
            float height,
            float radius
    ) throws IOException {

        float r =
                Math.min(
                        radius,
                        Math.min(
                                width / 2f,
                                height / 2f
                        )
                );

        float c =
                r * 0.55228475f;

        float right = x + width;
        float top = y + height;

        stream.moveTo(
                x + r,
                y
        );

        stream.lineTo(
                right - r,
                y
        );

        stream.curveTo(
                right - r + c,
                y,
                right,
                y + r - c,
                right,
                y + r
        );

        stream.lineTo(
                right,
                top - r
        );

        stream.curveTo(
                right,
                top - r + c,
                right - r + c,
                top,
                right - r,
                top
        );

        stream.lineTo(
                x + r,
                top
        );

        stream.curveTo(
                x + r - c,
                top,
                x,
                top - r + c,
                x,
                top - r
        );

        stream.lineTo(
                x,
                y + r
        );

        stream.curveTo(
                x,
                y + r - c,
                x + r - c,
                y,
                x + r,
                y
        );

        stream.closePath();
    }
}
