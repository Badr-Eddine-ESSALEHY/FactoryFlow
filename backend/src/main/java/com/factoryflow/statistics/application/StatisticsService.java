package com.factoryflow.statistics.application;

import com.factoryflow.report.persistence.KpiStatisticsPointProjection;
import com.factoryflow.report.persistence.MaintenanceReportRepository;
import com.factoryflow.shared.error.ApiErrorCode;
import com.factoryflow.shared.error.ApiException;
import com.factoryflow.statistics.api.StatisticsResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatisticsService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Africa/Casablanca");
    private final MaintenanceReportRepository reports;
    private final Clock clock;
    public StatisticsService(MaintenanceReportRepository reports, Clock clock) { this.reports = reports; this.clock = clock; }

    @Transactional(readOnly = true)
    public StatisticsResponse get(Long kpiDefinitionId, LocalDate requestedFrom, LocalDate requestedTo) {
        LocalDate to = requestedTo == null ? LocalDate.now(clock.withZone(BUSINESS_ZONE)) : requestedTo;
        LocalDate from = requestedFrom == null ? to.minusDays(6) : requestedFrom;
        if (from.isAfter(to)) throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR,
                "dateFrom must be on or before dateTo.");
        Map<Long, Accumulator> grouped = new LinkedHashMap<>();
        for (KpiStatisticsPointProjection point : reports.findConfirmedStatisticsPoints(from, to, kpiDefinitionId)) {
            grouped.computeIfAbsent(point.getKpiDefinitionId(), ignored -> new Accumulator(point)).add(point);
        }
        return new StatisticsResponse(from, to, grouped.values().stream().map(Accumulator::response).toList());
    }

    private static final class Accumulator {
        private final Long id; private final String code; private final String name; private final String unit;
        private final List<StatisticsResponse.Point> points = new ArrayList<>();
        private final Set<Long> reportIds = new LinkedHashSet<>();
        private BigDecimal sum = BigDecimal.ZERO; private BigDecimal min; private BigDecimal max; private BigDecimal latest;
        private long samples; private long missing;
        private Accumulator(KpiStatisticsPointProjection first) {
            id = first.getKpiDefinitionId(); code = first.getCode(); name = first.getDisplayName(); unit = first.getUnit();
        }
        private void add(KpiStatisticsPointProjection point) {
            points.add(new StatisticsResponse.Point(point.getEffectiveDate(), point.getReportId(), point.getValue()));
            reportIds.add(point.getReportId());
            if (point.getValue() == null) { missing++; return; }
            BigDecimal value = point.getValue(); latest = value; samples++; sum = sum.add(value);
            min = min == null || value.compareTo(min) < 0 ? value : min;
            max = max == null || value.compareTo(max) > 0 ? value : max;
        }
        private StatisticsResponse.KpiStatistics response() {
            BigDecimal average = samples == 0 ? null : sum.divide(BigDecimal.valueOf(samples), 6, RoundingMode.HALF_UP)
                    .stripTrailingZeros();
            return new StatisticsResponse.KpiStatistics(id, code, name, unit, latest, min, max, average,
                    samples, reportIds.size(), missing, List.copyOf(points));
        }
    }
}
