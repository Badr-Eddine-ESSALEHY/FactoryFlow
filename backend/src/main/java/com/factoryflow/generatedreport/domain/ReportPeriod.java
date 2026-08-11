package com.factoryflow.generatedreport.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public record ReportPeriod(LocalDate start, LocalDate end) {

    public ReportPeriod {
        if (start == null || end == null || start.isAfter(end)) {
            throw new IllegalArgumentException("A report period requires start on or before end");
        }
    }

    public static ReportPeriod validated(GeneratedReportType type, LocalDate start, LocalDate end) {
        ReportPeriod period = new ReportPeriod(start, end);
        switch (type) {
            case DAILY -> {
                if (!start.equals(end)) throw invalid(type);
            }
            case WEEKLY -> {
                if (start.getDayOfWeek() != DayOfWeek.MONDAY || !end.equals(start.plusDays(6))) throw invalid(type);
            }
            case MONTHLY -> {
                if (start.getDayOfMonth() != 1 || !end.equals(start.with(TemporalAdjusters.lastDayOfMonth()))) throw invalid(type);
            }
            case MANUAL -> { }
        }
        return period;
    }

    private static IllegalArgumentException invalid(GeneratedReportType type) {
        return new IllegalArgumentException("The supplied dates do not form a valid " + type + " calendar period");
    }
}
