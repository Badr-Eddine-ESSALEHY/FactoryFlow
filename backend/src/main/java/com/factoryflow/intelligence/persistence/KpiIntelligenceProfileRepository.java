package com.factoryflow.intelligence.persistence;
import com.factoryflow.intelligence.domain.KpiIntelligenceProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
public interface KpiIntelligenceProfileRepository extends JpaRepository<KpiIntelligenceProfile, Long> {
    @EntityGraph(attributePaths = "kpi")
    Optional<KpiIntelligenceProfile> findByKpiId(Long kpiId);
    boolean existsByKpiId(Long kpiId);
}
