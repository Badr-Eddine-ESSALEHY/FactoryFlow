package com.factoryflow.notification.api;

import com.factoryflow.notification.domain.UserNotification;
import java.time.Instant;

public record NotificationResponse(Long id, String type, String title, String message, Long relatedReportId,
                                   Long relatedGeneratedReportId, Instant createdAt, Instant readAt) {
    public static NotificationResponse from(UserNotification value) {
        return new NotificationResponse(value.getId(), value.getType().name(), value.getTitle(), value.getMessage(),
                value.getRelatedReportId(), value.getRelatedGeneratedReportId(), value.getCreatedAt(), value.getReadAt());
    }
}
