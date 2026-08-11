package com.factoryflow.report.persistence;

import com.factoryflow.report.domain.MaintenanceReport;
import java.util.Optional;
import java.time.LocalDate;
import java.util.List;
import com.factoryflow.report.domain.ReportStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

public interface MaintenanceReportRepository extends JpaRepository<MaintenanceReport, Long>, JpaSpecificationExecutor<MaintenanceReport> {

    Optional<MaintenanceReport> findByIdAndSubmittedById(Long id, Long submittedById);

    @EntityGraph(attributePaths = {"submittedBy", "entries", "entries.definition"})
    List<MaintenanceReport> findAllByStatusAndEffectiveDateBetweenOrderByEffectiveDateAscIdAsc(
            ReportStatus status,
            LocalDate periodStart,
            LocalDate periodEnd
    );

    long countByStatusAndEffectiveDate(ReportStatus status, LocalDate effectiveDate);

    @Query("select count(r) from MaintenanceReport r where r.effectiveDate = :date and r.status in :statuses")
    long countOpenOnDate(@Param("date") LocalDate date, @Param("statuses") List<ReportStatus> statuses);

    @Query("select count(e) from KpiEntry e where e.report.status = 'CONFIRMED' and e.report.effectiveDate = :date and e.finalValue is null")
    long countConfirmedMissingValues(@Param("date") LocalDate date);

    List<MaintenanceReport> findAllByOrderBySubmittedAtDesc(Pageable pageable);

    @Query(value = """
            SELECT DISTINCT ON (e.kpi_definition_id)
              e.kpi_definition_id AS kpiDefinitionId, d.code AS code, d.display_name AS displayName,
              d.unit AS unit, e.final_value AS value, r.effective_date AS effectiveDate,
              r.id AS reportId, r.confirmed_at AS confirmedAt
            FROM kpi_entries e
            JOIN maintenance_reports r ON r.id = e.report_id
            JOIN kpi_definitions d ON d.id = e.kpi_definition_id
            WHERE r.status = 'CONFIRMED' AND e.final_value IS NOT NULL
            ORDER BY e.kpi_definition_id, r.effective_date DESC, r.confirmed_at DESC, r.id DESC, e.id DESC
            """, nativeQuery = true)
    List<LatestKpiProjection> findLatestConfirmedKpiValues();

    @Query(value = """
            SELECT e.kpi_definition_id AS kpiDefinitionId, d.code AS code, d.display_name AS displayName,
              d.unit AS unit, r.effective_date AS effectiveDate, r.id AS reportId, e.final_value AS value
            FROM kpi_entries e
            JOIN maintenance_reports r ON r.id = e.report_id
            JOIN kpi_definitions d ON d.id = e.kpi_definition_id
            WHERE r.status = 'CONFIRMED'
              AND r.effective_date BETWEEN :dateFrom AND :dateTo
              AND (:kpiDefinitionId IS NULL OR e.kpi_definition_id = :kpiDefinitionId)
            ORDER BY d.display_name, r.effective_date, r.id, e.id
            """, nativeQuery = true)
    List<KpiStatisticsPointProjection> findConfirmedStatisticsPoints(
            @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo,
            @Param("kpiDefinitionId") Long kpiDefinitionId);
}
