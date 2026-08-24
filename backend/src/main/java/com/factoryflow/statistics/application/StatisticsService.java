package com.factoryflow.statistics.application;

import com.factoryflow.analytics.application.ReportAnalyticsService;
import com.factoryflow.analytics.application.ReportAnalyticsService.Measurement;
import com.factoryflow.analytics.domain.AnalyticsSnapshot;
import com.factoryflow.report.persistence.KpiStatisticsPointProjection;
import com.factoryflow.report.persistence.MaintenanceReportRepository;
import com.factoryflow.shared.error.ApiErrorCode;
import com.factoryflow.shared.error.ApiException;
import com.factoryflow.statistics.api.StatisticsResponse;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatisticsService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Africa/Casablanca");
    private final MaintenanceReportRepository reports;
    private final ReportAnalyticsService analytics;
    private final Clock clock;

    public StatisticsService(MaintenanceReportRepository reports, ReportAnalyticsService analytics, Clock clock) {
        this.reports = reports; this.analytics = analytics; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public StatisticsResponse get(Long kpiDefinitionId, LocalDate requestedFrom, LocalDate requestedTo) {
        LocalDate to = requestedTo == null ? LocalDate.now(clock.withZone(BUSINESS_ZONE)) : requestedTo;
        LocalDate from = requestedFrom == null ? to.minusDays(6) : requestedFrom;
        if (from.isAfter(to)) throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR,
                "dateFrom must be on or before dateTo.");
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        LocalDate previousTo = from.minusDays(1);
        LocalDate previousFrom = previousTo.minusDays(days - 1);
        AnalyticsSnapshot snapshot = analytics.analyze(
                measurements(reports.findConfirmedStatisticsPoints(from, to, kpiDefinitionId)),
                measurements(reports.findConfirmedStatisticsPoints(previousFrom, previousTo, kpiDefinitionId)));
        return new StatisticsResponse(from, to, snapshot.kpis().stream().map(this::response).toList());
    }

    private List<Measurement> measurements(List<KpiStatisticsPointProjection> points) {
        return points.stream().map(point -> new Measurement(point.getKpiDefinitionId(), point.getCode(),
                point.getDisplayName(), point.getUnit(), point.getEffectiveDate(), point.getReportId(), point.getValue())).toList();
    }

    private StatisticsResponse.KpiStatistics response(AnalyticsSnapshot.KpiAnalytics value) {
        return new StatisticsResponse.KpiStatistics(value.kpiDefinitionId(), value.code(), value.displayName(), value.unit(),
                value.latest(), value.minimum(), value.maximum(), value.mean(), value.range(), value.standardDeviation(),
                value.periodDelta(), value.trend().name(), value.first(), value.last(), value.validCount(), value.completenessRate(),
                value.validCount(), value.reportCount(), value.missingCount(), value.points().stream()
                .map(point -> new StatisticsResponse.Point(point.effectiveDate(), point.reportId(), point.value())).toList());
    }
}
