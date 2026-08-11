package com.factoryflow.report.persistence;

import com.factoryflow.report.domain.MaintenanceReport;
import java.util.Optional;
import java.time.LocalDate;
import java.util.List;
import com.factoryflow.report.domain.ReportStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MaintenanceReportRepository extends JpaRepository<MaintenanceReport, Long>, JpaSpecificationExecutor<MaintenanceReport> {

    Optional<MaintenanceReport> findByIdAndSubmittedById(Long id, Long submittedById);

    @EntityGraph(attributePaths = {"submittedBy", "entries", "entries.definition"})
    List<MaintenanceReport> findAllByStatusAndEffectiveDateBetweenOrderByEffectiveDateAscIdAsc(
            ReportStatus status,
            LocalDate periodStart,
            LocalDate periodEnd
    );
}
