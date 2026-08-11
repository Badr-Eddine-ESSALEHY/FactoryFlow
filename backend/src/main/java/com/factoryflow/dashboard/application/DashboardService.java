package com.factoryflow.dashboard.application;

import com.factoryflow.dashboard.api.DashboardResponse;
import com.factoryflow.generatedreport.persistence.GeneratedReportRepository;
import com.factoryflow.report.domain.ReportStatus;
import com.factoryflow.report.persistence.MaintenanceReportRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import com.factoryflow.schedule.application.ReportScheduleService;
import java.util.Comparator;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Africa/Casablanca");
    private final MaintenanceReportRepository reports;
    private final GeneratedReportRepository generatedReports;
    private final Clock clock;
    private final ReportScheduleService scheduleService;

    public DashboardService(MaintenanceReportRepository reports, GeneratedReportRepository generatedReports,
                            Clock clock, ReportScheduleService scheduleService) {
        this.reports = reports; this.generatedReports = generatedReports; this.clock = clock;
        this.scheduleService = scheduleService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse get() {
        LocalDate today = LocalDate.now(clock.withZone(BUSINESS_ZONE));
        long confirmed = reports.countByStatusAndEffectiveDate(ReportStatus.CONFIRMED, today);
        var latest = reports.findLatestConfirmedKpiValues().stream().map(value -> new DashboardResponse.LatestKpi(
                value.getKpiDefinitionId(), value.getCode(), value.getDisplayName(), value.getUnit(), value.getValue(),
                value.getEffectiveDate(), value.getReportId(), value.getConfirmedAt())).toList();
        var recent = reports.findAllByOrderBySubmittedAtDesc(PageRequest.of(0, 5)).stream().map(report ->
                new DashboardResponse.RecentReport(report.getId(), report.getEffectiveDate(), report.getStatus(),
                        report.getSubmittedAt(), report.getConfirmedAt(), report.getSubmittedBy().getName())).toList();
        var generated = generatedReports.findAllByOrderByGeneratedAtDesc(PageRequest.of(0, 5)).stream().map(report ->
                new DashboardResponse.RecentGeneratedReport(report.getId(), report.getType(), report.getFormat(),
                        report.getPeriodStart(), report.getPeriodEnd(), report.getGeneratedAt())).toList();
        var upcoming = scheduleService.list().stream().filter(value -> value.enabled() && value.nextRunAt() != null)
                .min(Comparator.comparing(value -> value.nextRunAt())).map(value ->
                        new DashboardResponse.UpcomingSchedule(value.id(), value.type(), value.nextRunAt(),
                                value.generateExcel(), value.generatePdf(), value.emailEnabled())).orElse(null);
        return new DashboardResponse(today, confirmed,
                reports.countOpenOnDate(today, List.of(ReportStatus.DRAFT, ReportStatus.PENDING_REVIEW)),
                reports.countConfirmedMissingValues(today), confirmed > 0, latest, recent, generated, upcoming);
    }
}
