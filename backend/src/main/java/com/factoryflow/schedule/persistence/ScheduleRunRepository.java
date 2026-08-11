package com.factoryflow.schedule.persistence;

import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.schedule.domain.ScheduleRun;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ScheduleRunRepository extends JpaRepository<ScheduleRun, Long> {
    Optional<ScheduleRun> findByScheduleIdAndPeriodStartAndPeriodEndAndFormat(
            Long scheduleId, LocalDate periodStart, LocalDate periodEnd, GeneratedReportFormat format);
    Page<ScheduleRun> findAllByScheduleId(Long scheduleId, Pageable pageable);
    java.util.List<ScheduleRun> findAllByScheduleId(Long scheduleId);
    @Transactional void deleteAllByScheduleId(Long scheduleId);
}
