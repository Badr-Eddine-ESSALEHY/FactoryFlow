package com.factoryflow.kpi.domain;

import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.shared.text.TextNormalizer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "kpi_aliases")
public class KpiAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "kpi_definition_id", nullable = false)
    private KpiDefinition definition;

    @Column(nullable = false, length = 200)
    private String alias;

    @Column(name = "normalized_alias", nullable = false, length = 200)
    private String normalizedAlias;

    @Column(nullable = false, length = 30)
    private String origin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private UserAccount approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected KpiAlias() {
    }

    KpiAlias(KpiDefinition definition, String alias) {
        this(definition, alias, null);
    }

    KpiAlias(KpiDefinition definition, String alias, UserAccount approvedBy) {
        this.definition = definition;
        this.alias = alias.trim();
        this.normalizedAlias = TextNormalizer.normalizeLabel(alias);
        this.origin = approvedBy == null ? "CONFIGURED" : "USER_APPROVED";
        this.approvedBy = approvedBy;
        this.approvedAt = approvedBy == null ? null : Instant.now();
        this.createdAt = Instant.now();
    }

    public String getAlias() { return alias; }
    public String getNormalizedAlias() { return normalizedAlias; }
    public String getOrigin() { return origin; }
}
