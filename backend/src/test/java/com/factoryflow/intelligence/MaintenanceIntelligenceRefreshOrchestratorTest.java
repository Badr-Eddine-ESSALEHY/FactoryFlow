package com.factoryflow.intelligence;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.factoryflow.intelligence.application.*;
import com.factoryflow.intelligence.domain.*;
import java.time.*;
import org.junit.jupiter.api.Test;
class MaintenanceIntelligenceRefreshOrchestratorTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-02-10T12:00:00Z"), ZoneOffset.UTC);
    @Test void notificationFailureDoesNotErasePersistedAnalysisOrAlert() {
        var profiles = mock(KpiIntelligenceProfileService.class); var engine = mock(MaintenanceIntelligenceService.class);
        var persistence = mock(MaintenanceIntelligencePersistenceService.class); var alerts = mock(ContextualAlertPersistenceService.class);
        var notifications = mock(IntelligenceAlertNotificationService.class); var profile = mock(KpiIntelligenceProfile.class);
        var result = mock(MaintenanceIntelligenceResult.class); var analysis = mock(MaintenanceIntelligenceAnalysis.class);
        when(profile.isEnabled()).thenReturn(true); when(profile.getHistoryWindowDays()).thenReturn(365);
        when(profile.getForecastHorizon()).thenReturn(7); when(profile.getExpectedCadenceDays()).thenReturn(null);
        when(profile.getSeasonalPeriod()).thenReturn(null); when(profiles.requireOrCreate(7L)).thenReturn(profile);
        when(engine.analyze(eq(7L), any(), any(), any())).thenReturn(result);
        when(persistence.persistSuccess(eq(profile), any(), any(), eq(result), any(), anyLong())).thenReturn(analysis);
        when(analysis.getId()).thenReturn(44L);
        var outcome = new ContextualAlertPersistenceService.AlertOutcome(55L, true, 2L);
        when(alerts.evaluateAndPersist(44L)).thenReturn(outcome); doThrow(new RuntimeException("notification unavailable")).when(notifications).notifyNewAlert(outcome);
        var orchestrator = new MaintenanceIntelligenceRefreshOrchestrator(profiles, engine, persistence, alerts, notifications, clock);
        assertThat(orchestrator.refresh(7L, LocalDate.of(2026, 2, 10), false)).isSameAs(analysis);
        verify(persistence).contextCompleted(44L); verify(persistence, never()).contextFailed(anyLong(), any());
    }
    @Test void runtimeFailureIsPersistedWithoutReplacingAPreviousSuccess() {
        var profiles = mock(KpiIntelligenceProfileService.class); var engine = mock(MaintenanceIntelligenceService.class);
        var persistence = mock(MaintenanceIntelligencePersistenceService.class); var profile = mock(KpiIntelligenceProfile.class);
        when(profile.isEnabled()).thenReturn(true); when(profile.getHistoryWindowDays()).thenReturn(365);
        when(profile.getForecastHorizon()).thenReturn(7); when(profile.getExpectedCadenceDays()).thenReturn(null);
        when(profile.getSeasonalPeriod()).thenReturn(null); when(profiles.requireOrCreate(7L)).thenReturn(profile);
        when(engine.analyze(eq(7L), any(), any(), any())).thenThrow(new MaintenanceIntelligenceProviderException("timeout"));
        var orchestrator = new MaintenanceIntelligenceRefreshOrchestrator(profiles, engine, persistence,
                mock(ContextualAlertPersistenceService.class), mock(IntelligenceAlertNotificationService.class), clock);
        assertThatThrownBy(() -> orchestrator.refresh(7L, null, false)).hasMessageContaining("could not complete");
        verify(persistence).persistFailure(eq(profile), any(), any(), any(), anyLong(), eq("ANALYTICAL_RUNTIME_FAILURE"), eq("timeout"));
        verify(persistence, never()).persistSuccess(any(), any(), any(), any(), any(), anyLong());
    }
}
