package com.factoryflow.intelligence.application;
import com.factoryflow.intelligence.domain.*;
import com.factoryflow.intelligence.persistence.*;
import com.factoryflow.report.persistence.*;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
public class ContextualAlertPersistenceService {
    private final MaintenanceIntelligenceAnalysisRepository analyses; private final MaintenanceIntelligenceAlertRepository alerts;
    private final KpiEntryRepository entries; private final MaintenanceReportRepository reports;
    private final ContextualIntelligenceDecisionEngine decisions; private final Clock clock;
    public ContextualAlertPersistenceService(MaintenanceIntelligenceAnalysisRepository analyses,
            MaintenanceIntelligenceAlertRepository alerts, KpiEntryRepository entries, MaintenanceReportRepository reports,
            ContextualIntelligenceDecisionEngine decisions, Clock clock) {
        this.analyses = analyses; this.alerts = alerts; this.entries = entries; this.reports = reports; this.decisions = decisions; this.clock = clock;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AlertOutcome evaluateAndPersist(Long analysisId) {
        MaintenanceIntelligenceAnalysis analysis = analyses.findById(analysisId).orElseThrow();
        var decision = decisions.decide(analysis);
        if (decision.isEmpty() || analysis.getLatestEntryId() == null || analysis.getLatestReportId() == null) return AlertOutcome.none();
        var d = decision.get(); var now = clock.instant();
        var existing = alerts.findBySourceEntryIdAndType(analysis.getLatestEntryId(), d.type());
        if (existing.isPresent()) {
            var alert = existing.get(); alert.updateEvidence(analysis, d.anomalySignal(), analysis.getLatestAnomalyScore(),
                    analysis.getExpectedValue(), analysis.getExpectedLowerBound(), analysis.getExpectedUpperBound(),
                    analysis.getOutsideExpectedInterval(), analysis.getTrendDirection(), analysis.getForecastDirection() == null ? "UNAVAILABLE" : analysis.getForecastDirection(),
                    analysis.getSelectedModelFamily(), now);
            alerts.save(alert); return new AlertOutcome(alert.getId(), false, alert.getSourceReport().getSubmittedBy().getId());
        }
        var entry = entries.findById(analysis.getLatestEntryId()).orElseThrow();
        var report = reports.findById(analysis.getLatestReportId()).orElseThrow();
        var alert = MaintenanceIntelligenceAlert.create(analysis.getKpi(), analysis, entry, report, d.type(), d.attentionLevel(),
                analysis.getLatestEffectiveDate(), analysis.getLatestActualValue(), d.anomalySignal(), analysis.getLatestAnomalyScore(),
                analysis.getExpectedValue(), analysis.getExpectedLowerBound(), analysis.getExpectedUpperBound(),
                analysis.getOutsideExpectedInterval(), analysis.getTrendDirection(), analysis.getForecastDirection() == null ? "UNAVAILABLE" : analysis.getForecastDirection(),
                analysis.getSelectedModelFamily(), now);
        alert = alerts.saveAndFlush(alert); return new AlertOutcome(alert.getId(), true, report.getSubmittedBy().getId());
    }
    public record AlertOutcome(Long alertId, boolean newlyCreated, Long recipientUserId) {
        static AlertOutcome none() { return new AlertOutcome(null, false, null); }
    }
}
