package com.factoryflow.kpi.domain;

import com.factoryflow.shared.text.TextNormalizer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected KpiAlias() {
    }

    KpiAlias(KpiDefinition definition, String alias) {
        this.definition = definition;
        this.alias = alias.trim();
        this.normalizedAlias = TextNormalizer.normalizeLabel(alias);
        this.createdAt = Instant.now();
    }

    public String getAlias() {
        return alias;
    }

    public String getNormalizedAlias() {
        return normalizedAlias;
    }
}
