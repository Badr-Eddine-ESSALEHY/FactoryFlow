package com.factoryflow.analytics.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AnalyticsSnapshot(long reportCount, long measurementCount, long missingValueCount,
                                BigDecimal completenessRate, List<KpiAnalytics> kpis) {
    public record KpiAnalytics(
            Long kpiDefinitionId, String code, String displayName, String unit,
            BigDecimal latest, BigDecimal mean, BigDecimal minimum, BigDecimal maximum,
            BigDecimal range, BigDecimal standardDeviation, BigDecimal periodDelta,
            TrendDirection trend, BigDecimal first, BigDecimal last,
            long validCount, long missingCount, long reportCount, BigDecimal completenessRate,
            List<Point> points
    ) { }
    public record Point(LocalDate effectiveDate, Long reportId, BigDecimal value) { }
}
