package com.factoryflow.notification.application;

import com.factoryflow.auth.application.AuthenticationService;
import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.notification.api.NotificationResponse;
import com.factoryflow.notification.domain.NotificationType;
import com.factoryflow.notification.domain.UserNotification;
import com.factoryflow.notification.persistence.UserNotificationRepository;
import com.factoryflow.shared.error.ApiErrorCode;
import com.factoryflow.shared.error.ApiException;
import java.time.Clock;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final UserNotificationRepository notifications; private final AuthenticationService authentication; private final Clock clock;
    public NotificationService(UserNotificationRepository notifications, AuthenticationService authentication, Clock clock) {
        this.notifications = notifications; this.authentication = authentication; this.clock = clock;
    }
    @Transactional(readOnly = true)
    public List<NotificationResponse> list(String email) {
        UserAccount user = authentication.requireUser(email);
        return notifications.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 100)).stream()
                .map(NotificationResponse::from).toList();
    }
    @Transactional
    public NotificationResponse markRead(String email, Long id) {
        UserAccount user = authentication.requireUser(email);
        UserNotification value = notifications.findByIdAndUserId(id, user.getId()).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.VALIDATION_ERROR, "Notification introuvable."));
        value.markRead(clock.instant()); return NotificationResponse.from(notifications.save(value));
    }
    @Transactional
    public void notify(UserAccount user, NotificationType type, String title, String message, Long reportId, Long generatedId) {
        if (user == null) return;
        notifications.save(UserNotification.create(user, type, title, message, reportId, generatedId, clock.instant()));
    }
    @Transactional
    public boolean notifyIntelligence(UserAccount user, String title, String message, Long reportId, Long alertId) {
        if (user == null || notifications.existsByRelatedIntelligenceAlertId(alertId)) return false;
        notifications.save(UserNotification.create(user, NotificationType.MAINTENANCE_INTELLIGENCE_ATTENTION,
                title, message, reportId, null, alertId, clock.instant()));
        return true;
    }
}
