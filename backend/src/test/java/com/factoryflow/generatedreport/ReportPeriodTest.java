package com.factoryflow.generatedreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.factoryflow.generatedreport.domain.GeneratedReportType;
import com.factoryflow.generatedreport.domain.ReportPeriod;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ReportPeriodTest {

    @Test
    void validatesDailyBoundary() {
        LocalDate date = LocalDate.of(2026, 8, 11);
        assertThat(ReportPeriod.validated(GeneratedReportType.DAILY, date, date))
                .isEqualTo(new ReportPeriod(date, date));
        assertThatThrownBy(() -> ReportPeriod.validated(GeneratedReportType.DAILY, date, date.plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validatesMondayThroughSundayWeeklyBoundary() {
        LocalDate monday = LocalDate.of(2026, 8, 10);
        assertThat(ReportPeriod.validated(GeneratedReportType.WEEKLY, monday, monday.plusDays(6)))
                .isEqualTo(new ReportPeriod(monday, LocalDate.of(2026, 8, 16)));
        assertThatThrownBy(() -> ReportPeriod.validated(
                GeneratedReportType.WEEKLY, monday.plusDays(1), monday.plusDays(7)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validatesCompleteCalendarMonthIncludingLeapFebruary() {
        assertThat(ReportPeriod.validated(
                GeneratedReportType.MONTHLY,
                LocalDate.of(2028, 2, 1),
                LocalDate.of(2028, 2, 29)
        )).isEqualTo(new ReportPeriod(LocalDate.of(2028, 2, 1), LocalDate.of(2028, 2, 29)));
        assertThatThrownBy(() -> ReportPeriod.validated(
                GeneratedReportType.MONTHLY,
                LocalDate.of(2026, 8, 2),
                LocalDate.of(2026, 8, 31)
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsArbitraryCustomDateRange() {
        assertThat(ReportPeriod.validated(
                GeneratedReportType.CUSTOM,
                LocalDate.of(2026, 8, 3),
                LocalDate.of(2026, 8, 11)
        )).isEqualTo(new ReportPeriod(LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 11)));
    }
}
