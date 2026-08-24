package com.factoryflow.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.factoryflow.analytics.application.ReportAnalyticsService;
import com.factoryflow.analytics.application.ReportAnalyticsService.Measurement;
import com.factoryflow.analytics.domain.TrendDirection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReportAnalyticsServiceTest {
    private final ReportAnalyticsService service = new ReportAnalyticsService();

    @Test
    void excludesMissingValuesButPreservesZeroAndComputesTraceableStatistics() {
        var result = service.analyze(List.of(
                point(1, "0"), point(2, null), point(3, "10"), point(4, "20")
        ), List.of(point(-1, "5"), point(0, "5")));
        var kpi = result.kpis().getFirst();
        assertThat(kpi.validCount()).isEqualTo(3);
        assertThat(kpi.missingCount()).isEqualTo(1);
        assertThat(kpi.first()).isEqualByComparingTo("0");
        assertThat(kpi.last()).isEqualByComparingTo("20");
        assertThat(kpi.mean()).isEqualByComparingTo("10");
        assertThat(kpi.minimum()).isEqualByComparingTo("0");
        assertThat(kpi.maximum()).isEqualByComparingTo("20");
        assertThat(kpi.range()).isEqualByComparingTo("20");
        assertThat(kpi.periodDelta()).isEqualByComparingTo("5");
        assertThat(kpi.standardDeviation()).isEqualByComparingTo("8.164966");
        assertThat(kpi.trend()).isEqualTo(TrendDirection.INCREASING);
        assertThat(kpi.completenessRate()).isEqualByComparingTo("75");
    }

    @Test
    void doesNotInventStatisticsWhenAllValuesAreMissing() {
        var kpi = service.analyze(List.of(point(1, null)), List.of()).kpis().getFirst();
        assertThat(kpi.mean()).isNull();
        assertThat(kpi.standardDeviation()).isNull();
        assertThat(kpi.periodDelta()).isNull();
        assertThat(kpi.trend()).isEqualTo(TrendDirection.INSUFFICIENT_DATA);
    }

    private Measurement point(int day, String value) {
        return new Measurement(1L, "VRAC", "Vrac", "t", LocalDate.of(2026, 1, 10).plusDays(day),
                (long) day + 10, value == null ? null : new BigDecimal(value));
    }
}
