package com.factoryflow.email.application;

import com.factoryflow.generatedreport.domain.GeneratedReport;
import java.util.List;
import java.util.Set;

public interface ScheduledReportEmailSender {
    void send(List<GeneratedReport> reports, Set<String> recipients);
}
