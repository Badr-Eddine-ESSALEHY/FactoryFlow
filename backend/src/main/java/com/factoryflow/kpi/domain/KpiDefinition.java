package com.factoryflow.kpi.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "kpi_definitions")
public class KpiDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String code;
    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;
    @Column(length = 150)
    private String category;
    @Column(length = 50)
    private String unit;
    @Column(name = "plausible_min", precision = 20, scale = 6)
    private BigDecimal plausibleMin;
    @Column(name = "plausible_max", precision = 20, scale = 6)
    private BigDecimal plausibleMax;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "definition", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<KpiAlias> aliases = new ArrayList<>();

    protected KpiDefinition() {
    }

    public static KpiDefinition create(
            String code,
            String displayName,
            String category,
            String unit,
            BigDecimal plausibleMin,
            BigDecimal plausibleMax,
            boolean active,
            List<String> aliases
    ) {
        if (plausibleMin != null && plausibleMax != null && plausibleMin.compareTo(plausibleMax) > 0) {
            throw new IllegalArgumentException("plausibleMin must be less than or equal to plausibleMax");
        }
        KpiDefinition definition = new KpiDefinition();
        definition.code = required(code).toUpperCase(Locale.ROOT);
        definition.displayName = required(displayName);
        definition.category = optional(category);
        definition.unit = optional(unit);
        definition.plausibleMin = plausibleMin;
        definition.plausibleMax = plausibleMax;
        definition.active = active;
        aliases.stream().map(String::trim).filter(value -> !value.isBlank()).distinct()
                .forEach(value -> definition.aliases.add(new KpiAlias(definition, value)));
        return definition;
    }

    @PrePersist
    void initializeTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
    public String getCategory() { return category; }
    public String getUnit() { return unit; }
    public BigDecimal getPlausibleMin() { return plausibleMin; }
    public BigDecimal getPlausibleMax() { return plausibleMax; }
    public boolean isActive() { return active; }
    public List<KpiAlias> getAliases() { return List.copyOf(aliases); }

    private static String required(String value) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Required text is missing");
        return value.trim();
    }

    private static String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
