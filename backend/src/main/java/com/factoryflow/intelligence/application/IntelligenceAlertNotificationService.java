package com.factoryflow.intelligence.application;
import com.factoryflow.auth.persistence.UserAccountRepository;
import com.factoryflow.intelligence.persistence.MaintenanceIntelligenceAlertRepository;
import com.factoryflow.notification.application.NotificationService;
import org.springframework.stereotype.Service;
@Service
public class IntelligenceAlertNotificationService {
    private final MaintenanceIntelligenceAlertRepository alerts; private final UserAccountRepository users;
    private final NotificationService notifications; private final AlertNotificationStateService states;
    public IntelligenceAlertNotificationService(MaintenanceIntelligenceAlertRepository alerts, UserAccountRepository users,
            NotificationService notifications, AlertNotificationStateService states) { this.alerts = alerts; this.users = users; this.notifications = notifications; this.states = states; }
    public void notifyNewAlert(ContextualAlertPersistenceService.AlertOutcome outcome) {
        if (!outcome.newlyCreated() || outcome.alertId() == null) return;
        try {
            var alert = alerts.findDetailedById(outcome.alertId()).orElseThrow(); var user = users.findById(outcome.recipientUserId()).orElseThrow();
            String title = alert.getAttentionLevel().name().equals("HIGH") ? "Écart contextuel important" : "Indicateur à examiner";
            String message = "Le KPI " + alert.getKpi().getDisplayName() + " présente un signal " + alert.getType().name()
                    + " pour l'observation du " + alert.getObservationDate() + ".";
            notifications.notifyIntelligence(user, title, message, alert.getSourceReport().getId(), alert.getId());
            states.sent(alert.getId());
        } catch (RuntimeException failure) {
            states.failed(outcome.alertId(), failure.getMessage());
            throw failure;
        }
    }
}
