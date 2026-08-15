package com.factoryflow.report.domain;

import com.factoryflow.kpi.domain.KpiDefinition;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "kpi_entries")
public class KpiEntry {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "report_id", nullable = false)
    private MaintenanceReport report;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "kpi_definition_id")
    private KpiDefinition definition;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "suggested_kpi_definition_id")
    private KpiDefinition suggestedDefinition;
    @Column(name = "suggestion_score", precision = 5, scale = 4)
    private BigDecimal suggestionScore;
    @Column(name = "source_label", length = 255) private String sourceLabel;
    @Column(name = "source_line") private String sourceLine;
    @Column(name = "extracted_value", precision = 20, scale = 6) private BigDecimal extractedValue;
    @Column(name = "current_value", precision = 20, scale = 6) private BigDecimal currentValue;
    @Column(name = "final_value", precision = 20, scale = 6) private BigDecimal finalValue;
    @Column(name = "secondary_extracted_value", precision = 20, scale = 6) private BigDecimal secondaryExtractedValue;
    @Column(name = "secondary_current_value", precision = 20, scale = 6) private BigDecimal secondaryCurrentValue;
    @Column(name = "secondary_final_value", precision = 20, scale = 6) private BigDecimal secondaryFinalValue;
    @Column(name = "secondary_unit", length = 50) private String secondaryUnit;
    @Column(name = "confidence_score", precision = 5, scale = 4) private BigDecimal confidenceScore;
    @Column(name = "edited_by_user", nullable = false) private boolean editedByUser;
    @Column(name = "captured_unit", length = 50) private String capturedUnit;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "kpi_entry_warnings", joinColumns = @JoinColumn(name = "kpi_entry_id"))
    @Column(name = "warning_code", nullable = false, length = 80)
    private Set<String> warningCodes = new LinkedHashSet<>();
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected KpiEntry() { }

    static KpiEntry draft(
            MaintenanceReport report, KpiDefinition definition, String sourceLabel, String sourceLine,
            BigDecimal extractedValue, BigDecimal currentValue, BigDecimal confidenceScore,
            boolean editedByUser, String capturedUnit, Set<String> warnings,
            KpiDefinition suggestedDefinition, BigDecimal suggestionScore,
            BigDecimal secondaryExtractedValue, BigDecimal secondaryCurrentValue, String secondaryUnit
    ) {
        KpiEntry entry = new KpiEntry();
        entry.report = report;
        entry.definition = definition;
        entry.suggestedDefinition = suggestedDefinition;
        entry.suggestionScore = suggestionScore;
        entry.sourceLabel = sourceLabel;
        entry.sourceLine = sourceLine;
        entry.extractedValue = extractedValue;
        entry.currentValue = currentValue;
        entry.secondaryExtractedValue = secondaryExtractedValue;
        entry.secondaryCurrentValue = secondaryCurrentValue;
        entry.secondaryUnit = secondaryUnit;
        entry.confidenceScore = confidenceScore;
        entry.editedByUser = editedByUser || !sameValue(extractedValue, currentValue);
        entry.capturedUnit = capturedUnit;
        entry.warningCodes.addAll(warnings);
        return entry;
    }

    @PrePersist void initializeTimestamps() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void updateTimestamp() { updatedAt = Instant.now(); }

    public void confirm(BigDecimal submittedFinalValue, BigDecimal submittedSecondaryFinalValue) {
        finalValue = submittedFinalValue;
        secondaryFinalValue = submittedSecondaryFinalValue;
        editedByUser = editedByUser || !sameValue(extractedValue, submittedFinalValue);
        editedByUser = editedByUser || !sameValue(secondaryExtractedValue, submittedSecondaryFinalValue);
    }

    public void confirm(BigDecimal submittedFinalValue) {
        confirm(submittedFinalValue, null);
    }

    private static boolean sameValue(BigDecimal left, BigDecimal right) {
        return left == null ? right == null : right != null && left.compareTo(right) == 0;
    }

    public Long getId() { return id; }
    public KpiDefinition getDefinition() { return definition; }
    public KpiDefinition getSuggestedDefinition() { return suggestedDefinition; }
    public BigDecimal getSuggestionScore() { return suggestionScore; }
    public String getSourceLabel() { return sourceLabel; }
    public String getSourceLine() { return sourceLine; }
    public BigDecimal getExtractedValue() { return extractedValue; }
    public BigDecimal getCurrentValue() { return currentValue; }
    public BigDecimal getFinalValue() { return finalValue; }
    public BigDecimal getSecondaryExtractedValue() { return secondaryExtractedValue; }
    public BigDecimal getSecondaryCurrentValue() { return secondaryCurrentValue; }
    public BigDecimal getSecondaryFinalValue() { return secondaryFinalValue; }
    public String getSecondaryUnit() { return secondaryUnit; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public boolean isEditedByUser() { return editedByUser; }
    public String getCapturedUnit() { return capturedUnit; }
    public Set<String> getWarningCodes() { return Set.copyOf(warningCodes); }
}
