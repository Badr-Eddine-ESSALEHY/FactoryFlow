package com.factoryflow.intelligence;

import static org.assertj.core.api.Assertions.assertThat;

import com.factoryflow.analytics.application.ReportAnalyticsService;
import com.factoryflow.analytics.domain.TrendDirection;
import com.factoryflow.intelligence.application.HistoricalTrendAnalyzer;
import com.factoryflow.intelligence.domain.KpiIdentity;
import com.factoryflow.intelligence.domain.PreparedKpiSeries;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class HistoricalTrendAnalyzerTest {
    private final HistoricalTrendAnalyzer analyzer = new HistoricalTrendAnalyzer(new ReportAnalyticsService());

    @Test
    void reusesIncreasingDecreasingStableAndInsufficientTrendSemantics() {
        assertThat(analyzer.analyze(series("1", "2", "3")).direction()).isEqualTo(TrendDirection.INCREASING);
        assertThat(analyzer.analyze(series("3", "2", "1")).direction()).isEqualTo(TrendDirection.DECREASING);
        assertThat(analyzer.analyze(series("5", "5", "5")).direction()).isEqualTo(TrendDirection.STABLE);
        assertThat(analyzer.analyze(series("5")).direction()).isEqualTo(TrendDirection.INSUFFICIENT_DATA);
    }

    @Test
    void exposesMagnitudeWithoutInventingPercentageFromZeroBaseline() {
        var increasing = analyzer.analyze(series("10", "15", "20"));
        assertThat(increasing.slopePerObservation()).isEqualByComparingTo("5");
        assertThat(increasing.absoluteChange()).isEqualByComparingTo("10");
        assertThat(increasing.percentageChange()).isEqualByComparingTo("100");
        assertThat(analyzer.analyze(series("0", "5")).percentageChange()).isNull();
    }

    private PreparedKpiSeries series(String... values) {
        LocalDate start = LocalDate.of(2026, 1, 1);
        List<PreparedKpiSeries.Observation> points = java.util.stream.IntStream.range(0, values.length)
                .mapToObj(index -> new PreparedKpiSeries.Observation((long) index + 1, (long) index + 100,
                        start.plusDays(index), Instant.parse("2026-01-01T00:00:00Z").plusSeconds(index),
                        new BigDecimal(values[index])))
                .toList();
        return new PreparedKpiSeries(new KpiIdentity(1L, "K", "KPI", "u"), start,
                start.plusDays(Math.max(0, values.length - 1)), values.length, values.length, 0,
                new PreparedKpiSeries.CadenceMetadata(
                        values.length < 2 ? PreparedKpiSeries.CadenceState.SINGLE_DATE : PreparedKpiSeries.CadenceState.REGULAR,
                        values.length < 2 ? null : 1, null,
                        values.length < 2 ? PreparedKpiSeries.CadenceBasis.UNKNOWN
                                : PreparedKpiSeries.CadenceBasis.INFERRED_OBSERVED,
                        values.length < 2 ? PreparedKpiSeries.CadenceAmbiguity.INSUFFICIENT_OBSERVATIONS
                                : PreparedKpiSeries.CadenceAmbiguity.NONE,
                        values.length, 0, 0, false, "NONE"), points);
    }
}
