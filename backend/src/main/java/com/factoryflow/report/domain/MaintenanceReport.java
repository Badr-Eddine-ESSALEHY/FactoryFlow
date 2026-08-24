package com.factoryflow.report.domain;

import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.kpi.domain.KpiDefinition;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "maintenance_reports")
public class MaintenanceReport {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "submitted_by", nullable = false)
    private UserAccount submittedBy;
    @Column(name = "effective_date", nullable = false) private LocalDate effectiveDate;
    @Column(name = "submitted_at", nullable = false, updatable = false) private Instant submittedAt;
    @Column(name = "raw_text") private String rawText;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private AcquisitionSource source;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private ReportStatus status;
    @Column(name = "confirmed_at") private Instant confirmedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version @Column(nullable = false) private long version;
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KpiEntry> entries = new ArrayList<>();
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportUnrecognizedLine> unrecognizedLines = new ArrayList<>();

    protected MaintenanceReport() { }

    public static MaintenanceReport draft(UserAccount user, LocalDate effectiveDate, AcquisitionSource source, String rawText) {
        MaintenanceReport report = new MaintenanceReport();
        report.submittedBy = user;
        report.effectiveDate = effectiveDate;
        report.source = source;
        report.rawText = rawText;
        report.status = ReportStatus.DRAFT;
        return report;
    }

    @PrePersist void initializeTimestamps() {
        Instant now = Instant.now(); submittedAt = now; createdAt = now; updatedAt = now;
    }
    @PreUpdate void updateTimestamp() { updatedAt = Instant.now(); }

    public void replaceDraft(LocalDate effectiveDate, AcquisitionSource source, String rawText) {
        requireEditable();
        this.effectiveDate = effectiveDate;
        this.source = source;
        this.rawText = rawText;
        entries.clear();
        unrecognizedLines.clear();
    }

    public void addEntry(KpiDefinition definition, String sourceLabel, String sourceLine, BigDecimal extractedValue,
                         BigDecimal currentValue, BigDecimal confidenceScore, boolean edited, String unit, Set<String> warnings) {
        requireEditable();
        addEntry(definition, sourceLabel, sourceLine, extractedValue, currentValue, confidenceScore, edited, unit,
                warnings, null, null, null, null, null, null, null);
    }

    public void addEntry(KpiDefinition definition, String sourceLabel, String sourceLine, BigDecimal extractedValue,
                         BigDecimal currentValue, BigDecimal confidenceScore, boolean edited, String unit, Set<String> warnings,
                         KpiDefinition suggestedDefinition, BigDecimal suggestionScore, String suggestionStrength,
                         String suggestionMatchMethod,
                         BigDecimal secondaryExtractedValue, BigDecimal secondaryCurrentValue, String secondaryUnit) {
        requireEditable();
        entries.add(KpiEntry.draft(this, definition, sourceLabel, sourceLine, extractedValue, currentValue,
                confidenceScore, edited, unit, warnings, suggestedDefinition, suggestionScore, suggestionStrength,
                suggestionMatchMethod,
                secondaryExtractedValue, secondaryCurrentValue, secondaryUnit));
    }

    public void addUnrecognizedLine(String sourceLine, UnknownLineResolution resolution, KpiDefinition definition) {
        addUnrecognizedLine(sourceLine, resolution, definition, UnknownLineKind.KPI_LIKE, "UNCLASSIFIED", false);
    }

    public void addUnrecognizedLine(String sourceLine, UnknownLineResolution resolution, KpiDefinition definition,
                                    UnknownLineKind kind, String classificationReason, boolean safeToIgnore) {
        requireEditable();
        unrecognizedLines.add(ReportUnrecognizedLine.draft(
                this, sourceLine, resolution, definition, kind, classificationReason, safeToIgnore));
    }

    public KpiEntry removeEntry(Long entryId) {
        requireEditable();
        KpiEntry entry = entries.stream()
                .filter(candidate -> candidate.getId().equals(entryId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Draft entry not found."));
        entries.remove(entry);
        return entry;
    }

    public void confirm() {
        requireEditable();
        status = ReportStatus.CONFIRMED;
        confirmedAt = Instant.now();
    }

    private void requireEditable() {
        if (status == ReportStatus.CONFIRMED) throw new IllegalStateException("Confirmed reports are immutable");
    }

    public Long getId() { return id; }
    public UserAccount getSubmittedBy() { return submittedBy; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public Instant getSubmittedAt() { return submittedAt; }
    public String getRawText() { return rawText; }
    public AcquisitionSource getSource() { return source; }
    public ReportStatus getStatus() { return status; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
    public List<KpiEntry> getEntries() { return entries; }
    public List<ReportUnrecognizedLine> getUnrecognizedLines() { return unrecognizedLines; }
}
