package com.factoryflow.notification.domain;

import com.factoryflow.auth.domain.UserAccount;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_notifications")
public class UserNotification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private UserAccount user;
    @Enumerated(EnumType.STRING) @Column(name = "notification_type", nullable = false, length = 50) private NotificationType type;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, length = 500) private String message;
    @Column(name = "related_report_id") private Long relatedReportId;
    @Column(name = "related_generated_report_id") private Long relatedGeneratedReportId;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "read_at") private Instant readAt;
    protected UserNotification() { }
    public static UserNotification create(UserAccount user, NotificationType type, String title, String message,
                                          Long reportId, Long generatedReportId, Instant createdAt) {
        UserNotification value = new UserNotification(); value.user = user; value.type = type; value.title = title;
        value.message = message; value.relatedReportId = reportId; value.relatedGeneratedReportId = generatedReportId;
        value.createdAt = createdAt; return value;
    }
    public void markRead(Instant at) { if (readAt == null) readAt = at; }
    public Long getId() { return id; } public NotificationType getType() { return type; }
    public String getTitle() { return title; } public String getMessage() { return message; }
    public Long getRelatedReportId() { return relatedReportId; } public Long getRelatedGeneratedReportId() { return relatedGeneratedReportId; }
    public Instant getCreatedAt() { return createdAt; } public Instant getReadAt() { return readAt; }
    public UserAccount getUser() { return user; }
}
