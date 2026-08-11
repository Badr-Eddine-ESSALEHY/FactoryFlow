package com.factoryflow.report.persistence;

import com.factoryflow.report.domain.MaintenanceReport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceReportRepository extends JpaRepository<MaintenanceReport, Long> {

    Optional<MaintenanceReport> findByIdAndSubmittedById(Long id, Long submittedById);
}
