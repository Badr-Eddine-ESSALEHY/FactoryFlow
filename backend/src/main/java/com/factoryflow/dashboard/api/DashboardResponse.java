package com.factoryflow.dashboard.api;

import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.generatedreport.domain.GeneratedReportType;
import com.factoryflow.report.domain.ReportStatus;
import com.factoryflow.schedule.domain.ReportScheduleType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public record DashboardResponse(
        LocalDate businessDate,
        long todayConfirmedReportCount,
        long todayDraftOrPendingReportCount,
        long todayConfirmedMissingValueCount,
        long todayGeneratedDocumentCount,
        boolean todayHasConfirmedReport,
        List<DailyActivity> activityTrend,
        List<LatestKpi> latestKpis,
        List<RecentReport> recentReports,
        List<RecentGeneratedReport> recentGeneratedReports,
        UpcomingSchedule upcomingSchedule
) {
    public record DailyActivity(LocalDate date, long confirmedReportCount, long missingValueCount) { }
    public record LatestKpi(Long kpiDefinitionId, String code, String displayName, String unit,
                            BigDecimal value, LocalDate effectiveDate, Long reportId, Instant confirmedAt) { }
    public record RecentReport(Long id, LocalDate effectiveDate, ReportStatus status, Instant submittedAt,
                               Instant confirmedAt, String submittedBy) { }
    public record RecentGeneratedReport(Long id, GeneratedReportType type, GeneratedReportFormat format,
                                        LocalDate periodStart, LocalDate periodEnd, Instant generatedAt) { }
    public record UpcomingSchedule(Long id, ReportScheduleType type, ZonedDateTime nextRunAt,
                                   boolean generateExcel, boolean generatePdf, boolean emailEnabled) { }
}
