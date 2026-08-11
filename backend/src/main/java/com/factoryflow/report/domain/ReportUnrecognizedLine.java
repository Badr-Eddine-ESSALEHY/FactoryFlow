package com.factoryflow.report.domain;

import com.factoryflow.kpi.domain.KpiDefinition;
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
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "report_unrecognized_lines")
public class ReportUnrecognizedLine {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "report_id", nullable = false)
    private MaintenanceReport report;
    @Column(name = "source_line", nullable = false) private String sourceLine;
    @Enumerated(EnumType.STRING) @Column(name = "resolution_status", nullable = false, length = 40)
    private UnknownLineResolution resolution;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "resolved_kpi_definition_id")
    private KpiDefinition resolvedDefinition;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected ReportUnrecognizedLine() { }

    static ReportUnrecognizedLine draft(MaintenanceReport report, String sourceLine,
                                        UnknownLineResolution resolution, KpiDefinition resolvedDefinition) {
        ReportUnrecognizedLine line = new ReportUnrecognizedLine();
        line.report = report;
        line.sourceLine = sourceLine.strip();
        line.resolve(resolution, resolvedDefinition);
        line.createdAt = Instant.now();
        return line;
    }

    public void resolve(UnknownLineResolution resolution, KpiDefinition definition) {
        if (resolution == UnknownLineResolution.ASSIGNED && definition == null) {
            throw new IllegalArgumentException("An assigned unknown line requires a KPI definition");
        }
        this.resolution = resolution;
        this.resolvedDefinition = resolution == UnknownLineResolution.ASSIGNED ? definition : null;
    }

    public Long getId() { return id; }
    public String getSourceLine() { return sourceLine; }
    public UnknownLineResolution getResolution() { return resolution; }
    public KpiDefinition getResolvedDefinition() { return resolvedDefinition; }
}
