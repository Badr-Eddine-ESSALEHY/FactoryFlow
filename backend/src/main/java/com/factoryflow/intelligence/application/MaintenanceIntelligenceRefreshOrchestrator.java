package com.factoryflow.intelligence.application;

import com.factoryflow.intelligence.domain.*;
import com.factoryflow.shared.error.*;
import java.time.*;
import org.slf4j.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MaintenanceIntelligenceRefreshOrchestrator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceIntelligenceRefreshOrchestrator.class);
    private final KpiIntelligenceProfileService profiles; private final MaintenanceIntelligenceService engine;
    private final MaintenanceIntelligencePersistenceService persistence; private final ContextualAlertPersistenceService alerts;
    private final IntelligenceAlertNotificationService notifications; private final Clock clock;
    public MaintenanceIntelligenceRefreshOrchestrator(KpiIntelligenceProfileService profiles, MaintenanceIntelligenceService engine,
            MaintenanceIntelligencePersistenceService persistence, ContextualAlertPersistenceService alerts,
            IntelligenceAlertNotificationService notifications, Clock clock) {
        this.profiles = profiles; this.engine = engine; this.persistence = persistence; this.alerts = alerts;
        this.notifications = notifications; this.clock = clock;
    }
    public MaintenanceIntelligenceAnalysis refresh(Long kpiId, LocalDate requestedEnd, boolean automatic) {
        KpiIntelligenceProfile profile = profiles.requireOrCreate(kpiId);
        if (!profile.isEnabled()) {
            if (automatic) return null;
            throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.INTELLIGENCE_DISABLED, "Maintenance Intelligence is disabled for this KPI.");
        }
        LocalDate end = requestedEnd == null ? LocalDate.now(clock) : requestedEnd;
        LocalDate start = end.minusDays(profile.getHistoryWindowDays() - 1L);
        long started = System.nanoTime();
        MaintenanceIntelligenceAnalysis persisted;
        try {
            var settings = new MaintenanceIntelligenceSettings(profile.getExpectedCadenceDays(), profile.getForecastHorizon(), profile.getSeasonalPeriod());
            var result = engine.analyze(kpiId, start, end, settings);
            persisted = persistence.persistSuccess(profile, start, end, result, clock.instant(), elapsedMillis(started));
        } catch (RuntimeException failure) {
            try { persistence.persistFailure(profile, start, end, clock.instant(), elapsedMillis(started),
                    failure instanceof MaintenanceIntelligenceProviderException ? "ANALYTICAL_RUNTIME_FAILURE" : "ANALYSIS_EXECUTION_FAILURE", failure.getMessage()); }
            catch (RuntimeException persistenceFailure) { failure.addSuppressed(persistenceFailure); }
            if (automatic) { LOGGER.error("Automatic MI refresh failed kpiId={}", kpiId, failure); return null; }
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, ApiErrorCode.INTELLIGENCE_RUNTIME_UNAVAILABLE,
                    "Maintenance Intelligence could not complete the refresh.");
        }
        try {
            var outcome = alerts.evaluateAndPersist(persisted.getId());
            persistence.contextCompleted(persisted.getId());
            try { notifications.notifyNewAlert(outcome); }
            catch (RuntimeException notificationFailure) { LOGGER.error("MI alert notification failed alertId={}", outcome.alertId(), notificationFailure); }
        } catch (RuntimeException contextualFailure) {
            persistence.contextFailed(persisted.getId(), contextualFailure.getMessage());
            LOGGER.error("MI contextualization failed analysisId={}", persisted.getId(), contextualFailure);
        }
        return persisted;
    }
    private long elapsedMillis(long started) { return (System.nanoTime() - started) / 1_000_000L; }
}
