package com.factoryflow.schedule.persistence;

import com.factoryflow.schedule.domain.ReportSchedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, Long> {
    List<ReportSchedule> findAllByOrderByTypeAscTimeAsc();
    List<ReportSchedule> findAllByEnabledTrue();
}
