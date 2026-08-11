package com.factoryflow.statistics.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StatisticsResponse(LocalDate dateFrom, LocalDate dateTo, List<KpiStatistics> kpis) {
    public record KpiStatistics(Long kpiDefinitionId, String code, String displayName, String unit,
                                BigDecimal latest, BigDecimal minimum, BigDecimal maximum, BigDecimal average,
                                long sampleCount, long reportCount, long missingValueCount, List<Point> points) { }
    public record Point(LocalDate effectiveDate, Long reportId, BigDecimal value) { }
}
