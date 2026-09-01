package com.factoryflow.intelligence.persistence;
import com.factoryflow.intelligence.domain.*;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.domain.Specification;
public interface MaintenanceIntelligenceAlertRepository extends JpaRepository<MaintenanceIntelligenceAlert, Long>,
        JpaSpecificationExecutor<MaintenanceIntelligenceAlert> {
    @EntityGraph(attributePaths = {"kpi", "analysis", "sourceEntry", "sourceReport"})
    @Query("select a from MaintenanceIntelligenceAlert a where a.id = :id")
    Optional<MaintenanceIntelligenceAlert> findDetailedById(@Param("id") Long id);
    Optional<MaintenanceIntelligenceAlert> findBySourceEntryIdAndType(Long entryId, ContextualAlertType type);
    @EntityGraph(attributePaths = {"kpi", "analysis", "sourceEntry", "sourceReport"})
    Page<MaintenanceIntelligenceAlert> findAll(Specification<MaintenanceIntelligenceAlert> specification,
                                               Pageable pageable);
    long countByKpiId(Long kpiId);
}
