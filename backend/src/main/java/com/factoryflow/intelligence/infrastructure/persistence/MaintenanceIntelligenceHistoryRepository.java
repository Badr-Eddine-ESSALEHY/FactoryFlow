package com.factoryflow.intelligence.infrastructure.persistence;

import com.factoryflow.report.domain.KpiEntry;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface MaintenanceIntelligenceHistoryRepository extends Repository<KpiEntry, Long> {

    @Query(value = """
            SELECT d.id AS kpiDefinitionId,
                   d.code AS code,
                   d.display_name AS displayName,
                   d.unit AS unit,
                   e.id AS entryId,
                   r.id AS reportId,
                   r.effective_date AS effectiveDate,
                   r.confirmed_at AS confirmedAt,
                   r.status AS reportStatus,
                   e.final_value AS finalValue
            FROM kpi_entries e
            JOIN maintenance_reports r ON r.id = e.report_id
            JOIN kpi_definitions d ON d.id = e.kpi_definition_id
            WHERE r.status = 'CONFIRMED'
              AND e.kpi_definition_id = :kpiDefinitionId
              AND r.effective_date BETWEEN :windowStart AND :windowEnd
            ORDER BY r.effective_date, r.confirmed_at, r.id, e.id
            """, nativeQuery = true)
    List<ConfirmedKpiHistoryProjection> findConfirmedHistory(
            @Param("kpiDefinitionId") Long kpiDefinitionId,
            @Param("windowStart") LocalDate windowStart,
            @Param("windowEnd") LocalDate windowEnd);
}
