package com.factoryflow.email.application;

import com.factoryflow.generatedreport.domain.GeneratedReport;
import com.factoryflow.generatedreport.persistence.GeneratedReportRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GeneratedReportEmailStateService {
    private final GeneratedReportRepository reports;
    public GeneratedReportEmailStateService(GeneratedReportRepository reports) { this.reports = reports; }
    @Transactional(readOnly = true)
    public List<GeneratedReport> getAll(List<Long> ids) {
        List<GeneratedReport> found = reports.findAllById(ids);
        if (found.size() != ids.size()) throw new IllegalStateException("Generated report email batch is incomplete");
        return found;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delivered(List<Long> ids) { reports.findAllById(ids).forEach(GeneratedReport::markEmailDelivered); }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failed(List<Long> ids) { reports.findAllById(ids).forEach(GeneratedReport::markEmailFailed); }
}
