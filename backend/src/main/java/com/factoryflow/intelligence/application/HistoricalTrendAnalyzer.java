package com.factoryflow.intelligence.application;

import com.factoryflow.analytics.application.ReportAnalyticsService;
import com.factoryflow.analytics.application.ReportAnalyticsService.TrendMeasurement;
import com.factoryflow.intelligence.domain.MaintenanceIntelligenceResult.TrendAnalysis;
import com.factoryflow.intelligence.domain.PreparedKpiSeries;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HistoricalTrendAnalyzer {
    private final ReportAnalyticsService analytics;

    public HistoricalTrendAnalyzer(ReportAnalyticsService analytics) {
        this.analytics = analytics;
    }

    public TrendAnalysis analyze(PreparedKpiSeries series) {
        TrendMeasurement trend = analytics.analyzeTrend(
                series.observations().stream().map(PreparedKpiSeries.Observation::value).toList());
        return new TrendAnalysis(trend.direction(), trend.slopePerObservation(), trend.absoluteChange(),
                trend.percentageChange(), trend.observationCount());
    }
}
