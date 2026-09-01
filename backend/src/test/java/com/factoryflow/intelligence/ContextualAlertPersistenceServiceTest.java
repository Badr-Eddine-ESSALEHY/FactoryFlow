package com.factoryflow.intelligence;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.intelligence.application.*;
import com.factoryflow.intelligence.domain.*;
import com.factoryflow.intelligence.persistence.*;
import com.factoryflow.report.domain.MaintenanceReport;
import com.factoryflow.report.persistence.*;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
class ContextualAlertPersistenceServiceTest {
    @Test void repeatedUnchangedObservationUpdatesEvidenceWithoutCreatingAnotherAlert() {
        var analyses = mock(MaintenanceIntelligenceAnalysisRepository.class); var alerts = mock(MaintenanceIntelligenceAlertRepository.class);
        var analysis = mock(MaintenanceIntelligenceAnalysis.class); var existing = mock(MaintenanceIntelligenceAlert.class);
        var report = mock(MaintenanceReport.class); var user = mock(UserAccount.class);
        when(analyses.findById(44L)).thenReturn(Optional.of(analysis)); when(analysis.getLatestEntryId()).thenReturn(9L);
        when(analysis.getLatestReportId()).thenReturn(8L); when(analysis.getLatestAnomalous()).thenReturn(true);
        when(analysis.getOutsideExpectedInterval()).thenReturn(false); when(analysis.getTrendDirection()).thenReturn(com.factoryflow.analytics.domain.TrendDirection.STABLE);
        when(alerts.findBySourceEntryIdAndType(9L, ContextualAlertType.ANOMALOUS_OBSERVATION)).thenReturn(Optional.of(existing));
        when(existing.getId()).thenReturn(77L); when(existing.getSourceReport()).thenReturn(report); when(report.getSubmittedBy()).thenReturn(user); when(user.getId()).thenReturn(2L);
        var service = new ContextualAlertPersistenceService(analyses, alerts, mock(KpiEntryRepository.class),
                mock(MaintenanceReportRepository.class), new ContextualIntelligenceDecisionEngine(), Clock.systemUTC());
        var outcome = service.evaluateAndPersist(44L);
        assertThat(outcome.alertId()).isEqualTo(77L); assertThat(outcome.newlyCreated()).isFalse();
        verify(existing).updateEvidence(eq(analysis), eq(true), any(), any(), any(), any(), eq(false), any(), any(), any(), any());
        verify(alerts).save(existing); verify(alerts, never()).saveAndFlush(any());
    }
}
