package com.factoryflow.email.infrastructure;

import com.factoryflow.email.application.ScheduledReportEmailSender;
import com.factoryflow.generatedreport.domain.GeneratedReport;
import com.factoryflow.generatedreport.storage.ReportStorageService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Set;
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
    public void send(GeneratedReport report, Set<String> recipients) {
        if (from == null || from.isBlank()) throw new IllegalStateException("MAIL_FROM is not configured");
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(recipients.toArray(String[]::new));
            DateTimeFormatter date = DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.FRENCH);
            String type = switch (report.getType()) {
                case DAILY -> "journalier"; case WEEKLY -> "hebdomadaire"; case MONTHLY -> "mensuel";
                case MANUAL -> "personnalisé";
            };
            String period = report.getPeriodStart().equals(report.getPeriodEnd())
                    ? report.getPeriodStart().format(date)
                    : "du " + report.getPeriodStart().format(date) + " au " + report.getPeriodEnd().format(date);
            helper.setSubject("FactoryFlow — Rapport de maintenance " + type + " — " + period);
            helper.setText("Bonjour,\n\nLe rapport de maintenance " + type + " " + period
                    + " a été généré automatiquement par FactoryFlow à partir des données confirmées."
                    + "\n\nLe document est joint à ce message. Les valeurs non renseignées restent explicitement identifiées et ne sont jamais assimilées à zéro."
                    + "\n\nCordialement,\nFactoryFlow — Alf Mabrouk");
            helper.addAttachment(report.getFileName(), storage.read(report.getFilePath()).resource());
            mailSender.send(message);
        } catch (MessagingException exception) {
            throw new IllegalStateException("Scheduled report email could not be prepared", exception);
        }
    }
}
