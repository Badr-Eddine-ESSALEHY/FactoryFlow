package com.factoryflow.schedule;

import static org.assertj.core.api.Assertions.assertThat;

import com.factoryflow.schedule.application.SchedulePeriodCalculator;
import com.factoryflow.schedule.domain.ReportScheduleType;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class SchedulePeriodCalculatorTest {
    private final SchedulePeriodCalculator calculator = new SchedulePeriodCalculator();
    @Test void dailyUsesPreviousCalendarDay() {
        assertThat(calculator.calculate(ReportScheduleType.DAILY, LocalDate.of(2026, 8, 12)))
                .extracting("start", "end").containsExactly(LocalDate.of(2026, 8, 11), LocalDate.of(2026, 8, 11));
    }
    @Test void weeklyUsesPreviousCompleteMondayThroughSunday() {
        assertThat(calculator.calculate(ReportScheduleType.WEEKLY, LocalDate.of(2026, 8, 12)))
                .extracting("start", "end").containsExactly(LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 9));
    }
    @Test void monthlyUsesPreviousCompleteMonth() {
        assertThat(calculator.calculate(ReportScheduleType.MONTHLY, LocalDate.of(2026, 8, 1)))
                .extracting("start", "end").containsExactly(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
    }
}
