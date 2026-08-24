package com.factoryflow.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.factoryflow.email.infrastructure.JavaMailScheduledReportEmailSender;
import com.factoryflow.generatedreport.domain.EmailDeliveryStatus;
import com.factoryflow.generatedreport.domain.GeneratedReport;
import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.generatedreport.domain.GeneratedReportType;
import com.factoryflow.generatedreport.domain.GenerationOrigin;
import com.factoryflow.generatedreport.domain.ReportPeriod;
import com.factoryflow.generatedreport.storage.ReportStorageService;
import com.factoryflow.generatedreport.storage.StoredReportFile;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;

class JavaMailScheduledReportEmailSenderTest {

    @Test
    void createsOneMultipartMessageWithPlainHtmlAndBothAttachments() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        ReportStorageService storage = mock(ReportStorageService.class);
        when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        when(storage.read("daily.xlsx"))
                .thenReturn(new StoredReportFile(new ByteArrayResource("excel".getBytes()), 5));
        when(storage.read("daily.pdf"))
                .thenReturn(new StoredReportFile(new ByteArrayResource("pdf".getBytes()), 3));

        var sender = new JavaMailScheduledReportEmailSender(
                mailSender,
                storage,
                "factoryflow@example.com"
        );

        sender.send(
                List.of(report(GeneratedReportFormat.PDF), report(GeneratedReportFormat.EXCEL)),
                Set.of("maintenance@example.com")
        );

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        verify(storage).read("daily.xlsx");
        verify(storage).read("daily.pdf");

        ByteArrayOutputStream serialized = new ByteArrayOutputStream();
        messageCaptor.getValue().writeTo(serialized);
        String mime = serialized.toString(StandardCharsets.UTF_8);

        assertThat(mime)
                .contains("multipart/mixed", "multipart/alternative")
                .contains("text/plain", "text/html")
                .contains("FactoryFlow", "Alf Mabrouk")
                .contains("daily.xlsx", "daily.pdf")
                .doesNotContain("MAIL_PASSWORD", "DB_PASSWORD", "smtp.gmail.com");
    }

    @Test
    void oneRequestedFormatProducesExactlyOneAttachment() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        ReportStorageService storage = mock(ReportStorageService.class);
        when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        when(storage.read("daily.pdf"))
                .thenReturn(new StoredReportFile(new ByteArrayResource("pdf".getBytes()), 3));
        var sender = new JavaMailScheduledReportEmailSender(
                mailSender,
                storage,
                "factoryflow@example.com"
        );

        sender.send(List.of(report(GeneratedReportFormat.PDF)), Set.of("maintenance@example.com"));

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        ByteArrayOutputStream serialized = new ByteArrayOutputStream();
        messageCaptor.getValue().writeTo(serialized);
        String mime = serialized.toString(StandardCharsets.UTF_8);
        assertThat(mime.split("Content-Disposition: attachment", -1)).hasSize(2);
        assertThat(mime).contains("daily.pdf").doesNotContain("daily.xlsx");
    }

    private GeneratedReport report(GeneratedReportFormat format) {
        LocalDate date = LocalDate.of(2026, 8, 21);
        String fileName = format == GeneratedReportFormat.EXCEL ? "daily.xlsx" : "daily.pdf";
        return GeneratedReport.ready(
                GeneratedReportType.DAILY,
                format,
                new ReportPeriod(date, date),
                Instant.parse("2026-08-21T17:00:00Z"),
                fileName,
                fileName,
                null,
                1,
                null,
                new LinkedHashSet<>(),
                GenerationOrigin.SCHEDULED,
                null,
                EmailDeliveryStatus.PENDING
        );
    }
}
