package com.factoryflow.intelligence;

import static org.mockito.Mockito.*;

import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.auth.persistence.UserAccountRepository;
import com.factoryflow.intelligence.application.AlertNotificationStateService;
import com.factoryflow.intelligence.application.ContextualAlertPersistenceService;
import com.factoryflow.intelligence.application.IntelligenceAlertNotificationService;
import com.factoryflow.intelligence.domain.ContextualAlertType;
import com.factoryflow.intelligence.domain.IntelligenceAttentionLevel;
import com.factoryflow.intelligence.domain.MaintenanceIntelligenceAlert;
import com.factoryflow.intelligence.persistence.MaintenanceIntelligenceAlertRepository;
import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.notification.application.NotificationService;
import com.factoryflow.report.domain.MaintenanceReport;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class IntelligenceAlertNotificationServiceTest {
    @Test
    void notificationUsesUserReadableSignalInsteadOfPersistedEnumName() {
        var alerts = mock(MaintenanceIntelligenceAlertRepository.class);
        var users = mock(UserAccountRepository.class);
        var notifications = mock(NotificationService.class);
        var states = mock(AlertNotificationStateService.class);
        var alert = mock(MaintenanceIntelligenceAlert.class);
        var kpi = mock(KpiDefinition.class);
        var report = mock(MaintenanceReport.class);
        var user = UserAccount.create("Engineer", "engineer@example.com", "hash");

        when(alerts.findDetailedById(12L)).thenReturn(Optional.of(alert));
        when(users.findById(3L)).thenReturn(Optional.of(user));
        when(alert.getId()).thenReturn(12L);
        when(alert.getAttentionLevel()).thenReturn(IntelligenceAttentionLevel.HIGH);
        when(alert.getType()).thenReturn(ContextualAlertType.STRONG_CONTEXTUAL_DEVIATION);
        when(alert.getKpi()).thenReturn(kpi);
        when(kpi.getDisplayName()).thenReturn("Consommation eau");
        when(alert.getObservationDate()).thenReturn(LocalDate.of(2026, 9, 1));
        when(alert.getSourceReport()).thenReturn(report);
        when(report.getId()).thenReturn(5L);

        new IntelligenceAlertNotificationService(alerts, users, notifications, states)
                .notifyNewAlert(new ContextualAlertPersistenceService.AlertOutcome(12L, true, 3L));

        verify(notifications).notifyIntelligence(
                user,
                "Écart contextuel important",
                "L'indicateur Consommation eau présente un écart confirmé par plusieurs signaux analytiques pour l'observation du 1 sept. 2026.",
                5L,
                12L);
        verify(states).sent(12L);
    }
}
