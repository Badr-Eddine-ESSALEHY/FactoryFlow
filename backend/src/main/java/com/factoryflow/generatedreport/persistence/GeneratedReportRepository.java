package com.factoryflow.generatedreport.persistence;

import com.factoryflow.generatedreport.domain.GeneratedReport;
import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.generatedreport.domain.GeneratedReportType;
import com.factoryflow.generatedreport.domain.GenerationOrigin;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, Long>, JpaSpecificationExecutor<GeneratedReport> {

    Optional<GeneratedReport> findFirstByTypeAndFormatAndPeriodStartAndPeriodEndAndOriginOrderByVersionDesc(
            GeneratedReportType type, GeneratedReportFormat format, LocalDate periodStart, LocalDate periodEnd,
            GenerationOrigin origin);

    long countByGeneratedAtGreaterThanEqualAndGeneratedAtLessThan(Instant from, Instant to);
    List<GeneratedReport> findAllByOrderByGeneratedAtDesc(Pageable pageable);
    List<GeneratedReport> findAllByScheduleId(Long scheduleId);
}
