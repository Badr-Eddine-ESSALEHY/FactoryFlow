package com.factoryflow.email.application;

import com.factoryflow.generatedreport.domain.EmailDeliveryStatus;
import com.factoryflow.generatedreport.domain.GeneratedReport;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailDeliveryService {
    private static final Logger log = LoggerFactory.getLogger(EmailDeliveryService.class);
    private final GeneratedReportEmailStateService state; private final ScheduledReportEmailSender sender;
    public EmailDeliveryService(GeneratedReportEmailStateService state, ScheduledReportEmailSender sender) {
        this.state = state; this.sender = sender;
    }
    public EmailDeliveryStatus deliver(Long generatedReportId, Set<String> recipients) {
        GeneratedReport report = state.get(generatedReportId);
        try {
            sender.send(report, recipients);
            state.delivered(generatedReportId);
            return EmailDeliveryStatus.DELIVERED;
        } catch (RuntimeException exception) {
            state.failed(generatedReportId);
            log.warn("Scheduled email failed for generatedReportId={}: {}", generatedReportId, exception.getMessage());
            return EmailDeliveryStatus.FAILED;
        }
    }
}
