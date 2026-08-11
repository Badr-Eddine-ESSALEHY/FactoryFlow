package com.factoryflow.email.application;

import com.factoryflow.generatedreport.domain.GeneratedReport;
import com.factoryflow.generatedreport.persistence.GeneratedReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GeneratedReportEmailStateService {
    private final GeneratedReportRepository reports;
    public GeneratedReportEmailStateService(GeneratedReportRepository reports) { this.reports = reports; }
    @Transactional(readOnly = true)
    public GeneratedReport get(Long id) { return reports.findById(id).orElseThrow(); }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delivered(Long id) { reports.findById(id).orElseThrow().markEmailDelivered(); }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failed(Long id) { reports.findById(id).orElseThrow().markEmailFailed(); }
}
