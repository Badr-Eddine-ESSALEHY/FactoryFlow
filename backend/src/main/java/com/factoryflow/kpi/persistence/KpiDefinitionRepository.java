package com.factoryflow.kpi.persistence;

import com.factoryflow.kpi.domain.KpiDefinition;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KpiDefinitionRepository extends JpaRepository<KpiDefinition, Long> {

    @EntityGraph(attributePaths = "aliases")
    List<KpiDefinition> findAllByActiveOrderByDisplayNameAsc(boolean active);

    @EntityGraph(attributePaths = "aliases")
    List<KpiDefinition> findAllByOrderByDisplayNameAsc();

    boolean existsByCodeIgnoreCase(String code);
}
