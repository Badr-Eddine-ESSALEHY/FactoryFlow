package com.factoryflow.email.application;

import com.factoryflow.generatedreport.domain.EmailDeliveryStatus;
import com.factoryflow.generatedreport.domain.GeneratedReport;
import java.util.List;
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
    public EmailDeliveryStatus deliver(List<Long> generatedReportIds, Set<String> recipients) {
        List<GeneratedReport> reports = state.getAll(generatedReportIds);
        try {
            sender.send(reports, recipients);
            state.delivered(generatedReportIds);
            return EmailDeliveryStatus.DELIVERED;
        } catch (RuntimeException exception) {
            state.failed(generatedReportIds);
            log.warn("Scheduled email failed for generatedReportIds={}: {}", generatedReportIds, exception.getMessage());
            return EmailDeliveryStatus.FAILED;
        }
    }

    public void markFailedWithoutDelivery(List<Long> generatedReportIds) {
        state.failed(generatedReportIds);
    }
}
