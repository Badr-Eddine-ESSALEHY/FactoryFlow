package com.factoryflow.generatedreport.application;

import com.factoryflow.generatedreport.domain.GeneratedReportType;
import com.factoryflow.report.domain.AcquisitionSource;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class ReportDocumentText {

    static final String BRAND = "ALF MABROUK";
    static final String PRODUCT = "FactoryFlow";
    static final String MISSING = "Non renseigné";
    static final String CONFIRMED = "Confirmé";
    static final ZoneId BUSINESS_ZONE = ZoneId.of("Africa/Casablanca");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.FRENCH);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("d MMMM uuuu 'à' HH:mm", Locale.FRENCH);

    private ReportDocumentText() { }

    static String title(GeneratedReportType type) {
        return switch (type) {
            case INDIVIDUAL -> "Rapport individuel de maintenance";
            case DAILY -> "Rapport journalier de maintenance";
            case WEEKLY -> "Rapport hebdomadaire de maintenance";
            case MONTHLY -> "Rapport mensuel de maintenance";
            case CUSTOM, MANUAL -> "Rapport personnalisé de maintenance";
        };
    }

    static String date(LocalDate value) {
        return DATE.format(value);
    }

    static String period(ReportGenerationData data) {
        if (data.period().start().equals(data.period().end())) return date(data.period().start());
        return "Du " + date(data.period().start()) + " au " + date(data.period().end());
    }

    static String instant(Instant value) {
        return value == null ? MISSING : DATE_TIME.format(value.atZone(BUSINESS_ZONE));
    }

    static String value(BigDecimal value) {
        if (value == null) return MISSING;
        BigDecimal normalized = value.stripTrailingZeros();
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.FRENCH);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');
        DecimalFormat format = new DecimalFormat("#,##0", symbols);
        format.setGroupingUsed(true);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(Math.max(0, normalized.scale()));
        return format.format(normalized);
    }

    static String unit(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    static String acquisitionSource(AcquisitionSource source) {
        if (source == null) return "—";
        return switch (source) {
            case PASTE -> "Texte collé";
            case MANUAL -> "Saisie manuelle";
            case GALLERY_OCR -> "Image importée";
            case SHARE_OCR -> "Partage Android";
        };
    }
}
