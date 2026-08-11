package com.factoryflow.schedule.domain;

import com.factoryflow.auth.domain.UserAccount;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "report_schedules")
public class ReportSchedule {
    public static final String BUSINESS_TIMEZONE = "Africa/Casablanca";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(name = "schedule_type", nullable = false, length = 30)
    private ReportScheduleType type;
    @Column(name = "time_of_day", nullable = false) private LocalTime time;
    @Enumerated(EnumType.STRING) @Column(name = "day_of_week", length = 20) private DayOfWeek dayOfWeek;
    @Column(nullable = false) private boolean enabled;
    @Column(name = "generate_excel", nullable = false) private boolean generateExcel;
    @Column(name = "generate_pdf", nullable = false) private boolean generatePdf;
    @Column(name = "email_enabled", nullable = false) private boolean emailEnabled;
    @Column(nullable = false, length = 100) private String timezone;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by") private UserAccount createdBy;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version @Column(nullable = false) private long version;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "report_schedule_recipients", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "email", nullable = false, length = 320)
    private Set<String> recipients = new LinkedHashSet<>();

    protected ReportSchedule() { }

    public static ReportSchedule create(UserAccount user, ReportScheduleType type, LocalTime time, DayOfWeek dayOfWeek,
                                        boolean generateExcel, boolean generatePdf, boolean emailEnabled,
                                        Set<String> recipients, boolean enabled) {
        ReportSchedule schedule = new ReportSchedule();
        schedule.createdBy = user;
        schedule.configure(type, time, dayOfWeek, generateExcel, generatePdf, emailEnabled, recipients, enabled);
        return schedule;
    }

    public void configure(ReportScheduleType type, LocalTime time, DayOfWeek dayOfWeek, boolean generateExcel,
                          boolean generatePdf, boolean emailEnabled, Set<String> recipients, boolean enabled) {
        if (type == null || time == null) throw new IllegalArgumentException("Schedule type and time are required");
        if (type == ReportScheduleType.WEEKLY && dayOfWeek == null) throw new IllegalArgumentException("Weekly schedules require dayOfWeek");
        if (type != ReportScheduleType.WEEKLY && dayOfWeek != null) throw new IllegalArgumentException("dayOfWeek is only valid for weekly schedules");
        if (!generateExcel && !generatePdf) throw new IllegalArgumentException("At least one report format is required");
        if (emailEnabled && recipients.isEmpty()) throw new IllegalArgumentException("Email-enabled schedules require at least one recipient");
        this.type = type; this.time = time; this.dayOfWeek = dayOfWeek; this.generateExcel = generateExcel;
        this.generatePdf = generatePdf; this.emailEnabled = emailEnabled; this.enabled = enabled;
        this.timezone = BUSINESS_TIMEZONE;
        this.recipients.clear(); this.recipients.addAll(recipients);
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    @PrePersist void initializeTimestamps() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void updateTimestamp() { updatedAt = Instant.now(); }
    public Long getId() { return id; }
    public ReportScheduleType getType() { return type; }
    public LocalTime getTime() { return time; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public boolean isEnabled() { return enabled; }
    public boolean isGenerateExcel() { return generateExcel; }
    public boolean isGeneratePdf() { return generatePdf; }
    public boolean isEmailEnabled() { return emailEnabled; }
    public String getTimezone() { return timezone; }
    public UserAccount getCreatedBy() { return createdBy; }
    public Set<String> getRecipients() { return Set.copyOf(recipients); }
    public long getVersion() { return version; }
}
