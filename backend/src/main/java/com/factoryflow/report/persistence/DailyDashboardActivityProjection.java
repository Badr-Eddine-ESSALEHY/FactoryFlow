package com.factoryflow.report.persistence;

import java.time.LocalDate;

public interface DailyDashboardActivityProjection {
    LocalDate getActivityDate();
    Long getConfirmedReportCount();
    Long getMissingValueCount();
}
