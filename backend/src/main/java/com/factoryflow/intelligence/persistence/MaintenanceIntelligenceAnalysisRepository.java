package com.factoryflow.intelligence.persistence;
import com.factoryflow.intelligence.domain.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
public interface MaintenanceIntelligenceAnalysisRepository extends JpaRepository<MaintenanceIntelligenceAnalysis, Long> {
    @EntityGraph(attributePaths = "kpi")
    Optional<MaintenanceIntelligenceAnalysis> findFirstByKpiIdOrderByGeneratedAtDesc(Long kpiId);
    @EntityGraph(attributePaths = "kpi")
    Optional<MaintenanceIntelligenceAnalysis> findFirstByKpiIdAndStatusInOrderByGeneratedAtDesc(Long kpiId, Collection<IntelligenceAnalysisStatus> statuses);
    @EntityGraph(attributePaths = "kpi")
    Page<MaintenanceIntelligenceAnalysis> findByKpiIdOrderByGeneratedAtDesc(Long kpiId, Pageable pageable);
    @EntityGraph(attributePaths = "kpi")
    Optional<MaintenanceIntelligenceAnalysis> findDetailedById(Long id);
}
