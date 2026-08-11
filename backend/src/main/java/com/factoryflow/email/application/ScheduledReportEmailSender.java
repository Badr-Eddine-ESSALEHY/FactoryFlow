package com.factoryflow.email.application;

import com.factoryflow.generatedreport.domain.GeneratedReport;
import java.util.Set;

public interface ScheduledReportEmailSender {
    void send(GeneratedReport report, Set<String> recipients);
}
