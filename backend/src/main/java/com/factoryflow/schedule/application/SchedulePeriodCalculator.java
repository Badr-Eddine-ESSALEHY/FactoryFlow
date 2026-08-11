package com.factoryflow.schedule.application;

import com.factoryflow.generatedreport.domain.ReportPeriod;
import com.factoryflow.schedule.domain.ReportScheduleType;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import org.springframework.stereotype.Component;

@Component
public class SchedulePeriodCalculator {
    public ReportPeriod calculate(ReportScheduleType type, LocalDate executionDate) {
        return switch (type) {
            case DAILY -> new ReportPeriod(executionDate.minusDays(1), executionDate.minusDays(1));
            case WEEKLY -> {
                LocalDate end = executionDate.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
                yield new ReportPeriod(end.minusDays(6), end);
            }
            case MONTHLY -> {
                LocalDate start = executionDate.withDayOfMonth(1).minusMonths(1);
                yield new ReportPeriod(start, start.with(TemporalAdjusters.lastDayOfMonth()));
            }
        };
    }
}
