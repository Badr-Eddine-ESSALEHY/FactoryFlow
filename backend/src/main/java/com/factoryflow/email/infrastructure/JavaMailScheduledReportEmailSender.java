package com.factoryflow.email.infrastructure;

import com.factoryflow.email.application.ScheduledReportEmailSender;
import com.factoryflow.generatedreport.domain.GeneratedReport;
import com.factoryflow.generatedreport.storage.ReportStorageService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.ZoneId;
import java.util.Set;
import java.util.List;
import java.util.Comparator;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class JavaMailScheduledReportEmailSender implements ScheduledReportEmailSender {
    private final JavaMailSender mailSender; private final ReportStorageService storage; private final String from;
    public JavaMailScheduledReportEmailSender(JavaMailSender mailSender, ReportStorageService storage,
                                              @Value("${factoryflow.mail.from:}") String from) {
        this.mailSender = mailSender; this.storage = storage; this.from = from;
    }
    @Override
    public void send(List<GeneratedReport> reports, Set<String> recipients) {
        if (from == null || from.isBlank()) throw new IllegalStateException("MAIL_FROM is not configured");
        if (reports == null || reports.isEmpty()) throw new IllegalArgumentException("At least one report is required");
        List<GeneratedReport> orderedReports = reports.stream()
                .sorted(Comparator.comparing(report -> report.getFormat().name()))
                .toList();
        GeneratedReport report = orderedReports.getFirst();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(recipients.toArray(String[]::new));
            DateTimeFormatter date = DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.FRENCH);
            String type = switch (report.getType()) {
                case INDIVIDUAL -> "individuel";
                case DAILY -> "journalier"; case WEEKLY -> "hebdomadaire"; case MONTHLY -> "mensuel";
                case CUSTOM, MANUAL -> "personnalisé";
            };
            String period = report.getPeriodStart().equals(report.getPeriodEnd())
                    ? report.getPeriodStart().format(date)
                    : "du " + report.getPeriodStart().format(date) + " au " + report.getPeriodEnd().format(date);
            helper.setSubject("FactoryFlow — Rapport de maintenance " + type + " — " + period);
            String attachmentLines = orderedReports.stream()
                    .map(value -> "- " + value.getFormat().name() + " (" + value.getFileName() + ")")
                    .collect(java.util.stream.Collectors.joining("\n"));
            String generatedAt = DateTimeFormatter.ofPattern("d MMMM uuuu 'à' HH:mm", Locale.FRENCH)
                    .format(report.getGeneratedAt().atZone(ZoneId.of("Africa/Casablanca")));
            String plainText = "Bonjour,\n\nLe rapport de maintenance " + type + " " + period
                    + " a été généré automatiquement par FactoryFlow à partir des données confirmées."
                    + "\n\nDocuments joints :\n" + attachmentLines
                    + "\n\nDate de génération : " + generatedAt
                    + "\nLes valeurs non renseignées restent distinctes de zéro."
                    + "\n\nCordialement,\nFactoryFlow — Alf Mabrouk";
            String formatBadges = orderedReports.stream()
                    .map(value -> "<span style=\"display:inline-block;padding:5px 10px;margin:0 6px 6px 0;"
                            + "background:#e7f0eb;color:#37654b;border-radius:12px;font-weight:700;\">"
                            + value.getFormat().name() + "</span>")
                    .collect(java.util.stream.Collectors.joining());
            String htmlText = """
                    <!doctype html><html><body style="margin:0;background:#f7f8f7;color:#30383c;font-family:Arial,sans-serif;">
                    <div style="max-width:640px;margin:0 auto;padding:24px 12px;">
                      <div style="background:#ffffff;border:1px solid #d9dedc;border-radius:16px;overflow:hidden;">
                        <div style="padding:22px 26px;background:#e7f0eb;border-bottom:3px solid #638b75;">
                          <div style="font-size:12px;letter-spacing:1.2px;color:#37654b;font-weight:700;">ALF MABROUK</div>
                          <div style="font-size:24px;font-weight:700;margin-top:4px;">FactoryFlow</div>
                        </div>
                        <div style="padding:26px;line-height:1.55;">
                          <p style="margin-top:0;">Bonjour,</p>
                          <p>Le rapport de maintenance <strong>%s</strong> pour la période <strong>%s</strong>
                          a été généré automatiquement à partir des données confirmées.</p>
                          <div style="background:#fcfbf8;border:1px solid #d9dedc;border-radius:12px;padding:16px;margin:20px 0;">
                            <div style="font-size:12px;color:#6c7672;text-transform:uppercase;font-weight:700;">Documents joints</div>
                            <div style="margin-top:10px;">%s</div>
                            <div style="font-size:13px;color:#6c7672;margin-top:8px;">Généré le %s</div>
                          </div>
                          <p style="font-size:13px;color:#6c7672;">Les valeurs non renseignées restent distinctes de zéro.</p>
                        </div>
                        <div style="padding:14px 26px;background:#fcfbf8;color:#6c7672;font-size:12px;">FactoryFlow · Alf Mabrouk</div>
                      </div>
                    </div></body></html>
                    """.formatted(type, period, formatBadges, generatedAt);
            helper.setText(plainText, htmlText);
            for (GeneratedReport attachment : orderedReports) {
                helper.addAttachment(attachment.getFileName(), storage.read(attachment.getFilePath()).resource());
            }
            mailSender.send(message);
        } catch (MessagingException exception) {
            throw new IllegalStateException("Scheduled report email could not be prepared", exception);
        }
    }
}
