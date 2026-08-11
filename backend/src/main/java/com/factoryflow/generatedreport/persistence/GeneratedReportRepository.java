package com.factoryflow.generatedreport.persistence;

import com.factoryflow.generatedreport.domain.GeneratedReport;
import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.generatedreport.domain.GeneratedReportType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, Long>, JpaSpecificationExecutor<GeneratedReport> {

    Optional<GeneratedReport> findFirstByTypeAndFormatAndPeriodStartAndPeriodEndOrderByVersionDesc(
            GeneratedReportType type,
            GeneratedReportFormat format,
            LocalDate periodStart,
            LocalDate periodEnd
    );
}
