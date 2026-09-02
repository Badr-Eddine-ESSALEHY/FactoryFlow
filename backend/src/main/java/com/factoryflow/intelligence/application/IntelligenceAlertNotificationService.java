package com.factoryflow.intelligence.application;
import com.factoryflow.auth.persistence.UserAccountRepository;
import com.factoryflow.intelligence.persistence.MaintenanceIntelligenceAlertRepository;
import com.factoryflow.notification.application.NotificationService;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Service;
@Service
public class IntelligenceAlertNotificationService {
    private static final DateTimeFormatter FRENCH_DATE = DateTimeFormatter.ofPattern("d MMM uuuu", Locale.FRENCH);
    private final MaintenanceIntelligenceAlertRepository alerts; private final UserAccountRepository users;
    private final NotificationService notifications; private final AlertNotificationStateService states;
    public IntelligenceAlertNotificationService(MaintenanceIntelligenceAlertRepository alerts, UserAccountRepository users,
            NotificationService notifications, AlertNotificationStateService states) { this.alerts = alerts; this.users = users; this.notifications = notifications; this.states = states; }
    public void notifyNewAlert(ContextualAlertPersistenceService.AlertOutcome outcome) {
        if (!outcome.newlyCreated() || outcome.alertId() == null) return;
        try {
            var alert = alerts.findDetailedById(outcome.alertId()).orElseThrow(); var user = users.findById(outcome.recipientUserId()).orElseThrow();
            String title = alert.getAttentionLevel().name().equals("HIGH") ? "Écart contextuel important" : "Indicateur à examiner";
            String signal = switch (alert.getType()) {
                case STRONG_CONTEXTUAL_DEVIATION -> "un écart confirmé par plusieurs signaux analytiques";
                case ANOMALOUS_OBSERVATION -> "une observation atypique";
                case FORECAST_DEVIATION -> "un écart à la prévision";
            };
            String message = "L'indicateur " + alert.getKpi().getDisplayName() + " présente " + signal
                    + " pour l'observation du " + alert.getObservationDate().format(FRENCH_DATE) + ".";
            notifications.notifyIntelligence(user, title, message, alert.getSourceReport().getId(), alert.getId());
            states.sent(alert.getId());
        } catch (RuntimeException failure) {
            states.failed(outcome.alertId(), failure.getMessage());
            throw failure;
        }
    }
}
