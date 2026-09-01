package com.factoryflow.analytics.application;

import com.factoryflow.analytics.domain.AnalyticsSnapshot;
import com.factoryflow.analytics.domain.TrendDirection;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ReportAnalyticsService {
    private static final int SCALE = 6;
    private static final BigDecimal STABLE_RELATIVE_SLOPE = new BigDecimal("0.01");
    private static final BigDecimal MINIMUM_STABLE_SLOPE = new BigDecimal("0.000001");

    public AnalyticsSnapshot analyze(List<Measurement> current, List<Measurement> previous) {
        Map<String, List<Measurement>> previousByKey = group(previous);
        List<AnalyticsSnapshot.KpiAnalytics> kpis = group(current).entrySet().stream()
                .map(entry -> summarize(entry.getValue(), previousByKey.getOrDefault(entry.getKey(), List.of())))
                .toList();
        Set<Long> reportIds = new LinkedHashSet<>();
        current.forEach(point -> reportIds.add(point.reportId()));
        long missing = current.stream().filter(point -> point.value() == null).count();
        long valid = current.size() - missing;
        return new AnalyticsSnapshot(reportIds.size(), valid, missing, ratio(valid, current.size()), kpis);
    }

    private Map<String, List<Measurement>> group(List<Measurement> values) {
        Map<String, List<Measurement>> grouped = new LinkedHashMap<>();
        values.stream().sorted(Comparator.comparing(Measurement::displayName).thenComparing(Measurement::effectiveDate)
                        .thenComparing(Measurement::reportId))
                .forEach(value -> grouped.computeIfAbsent(value.key(), ignored -> new ArrayList<>()).add(value));
        return grouped;
    }

    private AnalyticsSnapshot.KpiAnalytics summarize(List<Measurement> values, List<Measurement> previous) {
        Measurement identity = values.getFirst();
        List<Measurement> valid = values.stream().filter(value -> value.value() != null).toList();
        List<BigDecimal> numbers = valid.stream().map(Measurement::value).toList();
        BigDecimal mean = mean(numbers);
        BigDecimal minimum = numbers.stream().min(BigDecimal::compareTo).orElse(null);
        BigDecimal maximum = numbers.stream().max(BigDecimal::compareTo).orElse(null);
        BigDecimal first = numbers.isEmpty() ? null : numbers.getFirst();
        BigDecimal last = numbers.isEmpty() ? null : numbers.getLast();
        BigDecimal previousMean = mean(previous.stream().map(Measurement::value).filter(java.util.Objects::nonNull).toList());
        Set<Long> reportIds = new LinkedHashSet<>();
        values.forEach(value -> reportIds.add(value.reportId()));
        return new AnalyticsSnapshot.KpiAnalytics(
                identity.kpiDefinitionId(), identity.code(), identity.displayName(), identity.unit(),
                last, mean, minimum, maximum,
                minimum == null ? null : maximum.subtract(minimum), standardDeviation(numbers, mean),
                mean == null || previousMean == null ? null : mean.subtract(previousMean), analyzeTrend(numbers).direction(), first, last,
                valid.size(), values.size() - valid.size(), reportIds.size(), ratio(valid.size(), values.size()),
                valid.stream().map(value -> new AnalyticsSnapshot.Point(value.effectiveDate(), value.reportId(), value.value())).toList());
    }

    private BigDecimal mean(List<BigDecimal> values) {
        if (values.isEmpty()) return null;
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), SCALE, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    private BigDecimal standardDeviation(List<BigDecimal> values, BigDecimal mean) {
        if (values.size() < 2) return null;
        BigDecimal variance = values.stream().map(value -> value.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), SCALE * 2, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue())).setScale(SCALE, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    public TrendMeasurement analyzeTrend(List<BigDecimal> values) {
        if (values.size() < 2) {
            return new TrendMeasurement(TrendDirection.INSUFFICIENT_DATA, null, null, null, values.size());
        }
        BigDecimal mean = mean(values);
        BigDecimal xMean = BigDecimal.valueOf(values.size() - 1).divide(BigDecimal.valueOf(2), MathContext.DECIMAL64);
        BigDecimal numerator = BigDecimal.ZERO;
        BigDecimal denominator = BigDecimal.ZERO;
        for (int index = 0; index < values.size(); index++) {
            BigDecimal dx = BigDecimal.valueOf(index).subtract(xMean);
            numerator = numerator.add(dx.multiply(values.get(index).subtract(mean)));
            denominator = denominator.add(dx.pow(2));
        }
        BigDecimal slope = numerator.divide(denominator, SCALE * 2, RoundingMode.HALF_UP);
        BigDecimal threshold = mean.abs().multiply(STABLE_RELATIVE_SLOPE).max(MINIMUM_STABLE_SLOPE);
        TrendDirection direction = slope.abs().compareTo(threshold) <= 0
                ? TrendDirection.STABLE
                : (slope.signum() > 0 ? TrendDirection.INCREASING : TrendDirection.DECREASING);
        BigDecimal absoluteChange = values.getLast().subtract(values.getFirst());
        BigDecimal percentageChange = values.getFirst().compareTo(BigDecimal.ZERO) == 0
                ? null
                : absoluteChange.multiply(BigDecimal.valueOf(100))
                        .divide(values.getFirst().abs(), SCALE, RoundingMode.HALF_UP).stripTrailingZeros();
        return new TrendMeasurement(direction, slope.stripTrailingZeros(), absoluteChange, percentageChange, values.size());
    }

    private BigDecimal ratio(long numerator, long denominator) {
        if (denominator == 0) return null;
        return BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    public record Measurement(Long kpiDefinitionId, String code, String displayName, String unit,
                              LocalDate effectiveDate, Long reportId, BigDecimal value) {
        public String key() { return kpiDefinitionId == null ? displayName : kpiDefinitionId.toString(); }
    }

    public record TrendMeasurement(
            TrendDirection direction,
            BigDecimal slopePerObservation,
            BigDecimal absoluteChange,
            BigDecimal percentageChange,
            int observationCount
    ) {
    }
}
