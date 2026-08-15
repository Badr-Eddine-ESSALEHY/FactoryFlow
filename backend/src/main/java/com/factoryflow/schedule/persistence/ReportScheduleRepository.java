package com.factoryflow.schedule.persistence;

import com.factoryflow.schedule.domain.ReportSchedule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, Long> {
    List<ReportSchedule> findAllByOrderByTypeAscTimeAsc();
    List<ReportSchedule> findAllByEnabledTrue();
    @Query("select s from ReportSchedule s left join fetch s.createdBy where s.id = :id")
    Optional<ReportSchedule> findForExecution(@Param("id") Long id);
}
