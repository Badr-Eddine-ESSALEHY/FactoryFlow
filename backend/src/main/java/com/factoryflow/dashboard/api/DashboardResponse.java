package com.factoryflow.dashboard.api;

import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.generatedreport.domain.GeneratedReportType;
import com.factoryflow.report.domain.ReportStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.time.ZonedDateTime;
import com.factoryflow.schedule.domain.ReportScheduleType;

public record DashboardResponse(
        LocalDate businessDate,
        long todayConfirmedReportCount,
        long todayDraftOrPendingReportCount,
        long todayConfirmedMissingValueCount,
        boolean todayHasConfirmedReport,
        List<LatestKpi> latestKpis,
        List<RecentReport> recentReports,
        List<RecentGeneratedReport> recentGeneratedReports,
        UpcomingSchedule upcomingSchedule
) {
    public record LatestKpi(Long kpiDefinitionId, String code, String displayName, String unit,
                            BigDecimal value, LocalDate effectiveDate, Long reportId, Instant confirmedAt) { }
    public record RecentReport(Long id, LocalDate effectiveDate, ReportStatus status, Instant submittedAt,
                               Instant confirmedAt, String submittedBy) { }
    public record RecentGeneratedReport(Long id, GeneratedReportType type, GeneratedReportFormat format,
                                        LocalDate periodStart, LocalDate periodEnd, Instant generatedAt) { }
    public record UpcomingSchedule(Long id, ReportScheduleType type, ZonedDateTime nextRunAt,
                                   boolean generateExcel, boolean generatePdf, boolean emailEnabled) { }
}
