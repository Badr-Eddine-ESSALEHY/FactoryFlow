package com.factoryflow.generatedreport.domain;

import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.report.domain.MaintenanceReport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "generated_reports")
public class GeneratedReport {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private GeneratedReportType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private GeneratedReportFormat format;
    @Column(name = "period_start", nullable = false) private LocalDate periodStart;
    @Column(name = "period_end", nullable = false) private LocalDate periodEnd;
    @Column(name = "generated_at", nullable = false) private Instant generatedAt;
    @Column(name = "file_path", nullable = false) private String filePath;
    @Column(name = "file_name", nullable = false, length = 255) private String fileName;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "generated_by") private UserAccount generatedBy;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private GenerationOrigin origin;
    @Enumerated(EnumType.STRING) @Column(name = "generation_status", nullable = false, length = 40)
    private GenerationStatus generationStatus;
    @Enumerated(EnumType.STRING) @Column(name = "email_delivery_status", nullable = false, length = 40)
    private EmailDeliveryStatus emailDeliveryStatus;
    @Column(nullable = false) private int version;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "regenerated_from_id") private GeneratedReport regeneratedFrom;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @ManyToMany
    @JoinTable(name = "generated_report_sources",
            joinColumns = @JoinColumn(name = "generated_report_id"),
            inverseJoinColumns = @JoinColumn(name = "maintenance_report_id"))
    private Set<MaintenanceReport> sourceReports = new LinkedHashSet<>();

    protected GeneratedReport() { }

    public static GeneratedReport ready(GeneratedReportType type, GeneratedReportFormat format, ReportPeriod period,
                                        Instant generatedAt, String filePath, String fileName, UserAccount user,
                                        int version, GeneratedReport previous, Set<MaintenanceReport> sources) {
        GeneratedReport report = new GeneratedReport();
        report.type = type;
        report.format = format;
        report.periodStart = period.start();
        report.periodEnd = period.end();
        report.generatedAt = generatedAt;
        report.filePath = filePath;
        report.fileName = fileName;
        report.generatedBy = user;
        report.origin = GenerationOrigin.MANUAL;
        report.generationStatus = GenerationStatus.READY;
        report.emailDeliveryStatus = EmailDeliveryStatus.NOT_REQUESTED;
        report.version = version;
        report.regeneratedFrom = previous;
        report.createdAt = generatedAt;
        report.sourceReports.addAll(sources);
        return report;
    }

    public Long getId() { return id; }
    public GeneratedReportType getType() { return type; }
    public GeneratedReportFormat getFormat() { return format; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public Instant getGeneratedAt() { return generatedAt; }
    public String getFilePath() { return filePath; }
    public String getFileName() { return fileName; }
    public UserAccount getGeneratedBy() { return generatedBy; }
    public GenerationOrigin getOrigin() { return origin; }
    public GenerationStatus getGenerationStatus() { return generationStatus; }
    public EmailDeliveryStatus getEmailDeliveryStatus() { return emailDeliveryStatus; }
    public int getVersion() { return version; }
    public GeneratedReport getRegeneratedFrom() { return regeneratedFrom; }
    public Set<MaintenanceReport> getSourceReports() { return Set.copyOf(sourceReports); }
}
