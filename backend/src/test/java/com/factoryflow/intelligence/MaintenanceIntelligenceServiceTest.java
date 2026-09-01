package com.factoryflow.intelligence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.factoryflow.analytics.application.ReportAnalyticsService;
import com.factoryflow.intelligence.application.AnalyticalRuntimeRequest;
import com.factoryflow.intelligence.application.AnalyticalRuntimeResponse;
import com.factoryflow.intelligence.application.HistoricalKpiDataPreparer;
import com.factoryflow.intelligence.application.HistoricalTrendAnalyzer;
import com.factoryflow.intelligence.application.MaintenanceIntelligenceProperties;
import com.factoryflow.intelligence.application.MaintenanceIntelligenceProvider;
import com.factoryflow.intelligence.application.MaintenanceIntelligenceProviderException;
import com.factoryflow.intelligence.application.MaintenanceIntelligenceService;
import com.factoryflow.intelligence.domain.ConfirmedKpiHistoryRecord;
import com.factoryflow.intelligence.domain.KpiIdentity;
import com.factoryflow.intelligence.domain.MaintenanceIntelligenceResult;
import com.factoryflow.intelligence.domain.PreparedKpiSeries;
import com.factoryflow.report.domain.ReportStatus;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class MaintenanceIntelligenceServiceTest {
    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final Instant NOW = Instant.parse("2026-02-01T12:00:00Z");
    private static final KpiIdentity KPI = new KpiIdentity(7L, "TEMP", "Température", "°C");

    @Test
    void orchestratesPreparedConfirmedHistoryTrendAndRuntimeResult() throws Exception {
        AtomicReference<AnalyticalRuntimeRequest> captured = new AtomicReference<>();
        MaintenanceIntelligenceProvider provider = request -> {
            captured.set(request);
            return response(request, request.observations().stream()
                    .map(point -> new MaintenanceIntelligenceResult.AnomalyPoint(
                            point.entryId(), point.reportId(), point.effectiveDate(), point.confirmedAt(), point.value(),
                            BigDecimal.ZERO, BigDecimal.ZERO, false))
                    .toList());
        };
        var service = service(provider);
        var result = service.analyze(7L, START, START.plusDays(2));

        assertThat(result.generatedAt()).isEqualTo(NOW);
        assertThat(result.preparation().sourceRecordCount()).isEqualTo(3);
        assertThat(result.preparation().usableObservationCount()).isEqualTo(2);
        assertThat(result.preparation().missingValueCount()).isEqualTo(1);
        assertThat(captured.get().observations()).extracting(point -> point.value())
                .containsExactly(BigDecimal.ZERO, BigDecimal.TEN);
        assertThat(captured.get().observations()).extracting(point -> point.entryId()).containsExactly(1L, 3L);
        assertThat(result.trend().direction()).isEqualTo(com.factoryflow.analytics.domain.TrendDirection.INCREASING);
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules();
        var restored = mapper.treeToValue(mapper.valueToTree(result), MaintenanceIntelligenceResult.class);
        assertThat(restored.historicalObservations()).extracting(point -> point.entryId()).containsExactly(1L, 3L);
        assertThat(restored.latestObservationExpectation().entryId()).isEqualTo(3L);
    }

    @Test
    void rejectsRuntimeResponsesThatChangeSourceTraceability() {
        MaintenanceIntelligenceProvider provider = request -> response(request, List.of(
                new MaintenanceIntelligenceResult.AnomalyPoint(
                        999L, 101L, START, NOW, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false)));
        assertThatThrownBy(() -> service(provider).analyze(7L, START, START.plusDays(2)))
                .isInstanceOf(MaintenanceIntelligenceProviderException.class)
                .hasMessageContaining("changed or invented");
    }

    @Test
    void configuredExpectedCadenceAndProfileForecastSettingsReachTheRuntimeContract() {
        AtomicReference<AnalyticalRuntimeRequest> captured = new AtomicReference<>();
        MaintenanceIntelligenceProvider provider = request -> { captured.set(request); return response(request,
                request.observations().stream().map(point -> new MaintenanceIntelligenceResult.AnomalyPoint(
                        point.entryId(), point.reportId(), point.effectiveDate(), point.confirmedAt(), point.value(),
                        BigDecimal.ZERO, BigDecimal.ZERO, false)).toList()); };
        service(provider).analyze(7L, START, START.plusDays(2),
                new com.factoryflow.intelligence.application.MaintenanceIntelligenceSettings(1, 3, 5));
        assertThat(captured.get().cadence().expectedCadenceDays()).isEqualTo(1);
        assertThat(captured.get().cadence().cadenceBasis()).isEqualTo(PreparedKpiSeries.CadenceBasis.CONFIGURED_EXPECTED);
        assertThat(captured.get().cadence().ambiguity()).isEqualTo(PreparedKpiSeries.CadenceAmbiguity.MISSING_OBSERVATIONS);
        assertThat(captured.get().configuration().forecastHorizon()).isEqualTo(3);
        assertThat(captured.get().configuration().seasonalPeriod()).isEqualTo(5);
    }

    private MaintenanceIntelligenceService service(MaintenanceIntelligenceProvider provider) {
        return new MaintenanceIntelligenceService(
                (kpi, from, to) -> List.of(
                        history(1, 101, 0, "0"), history(2, 102, 1, null), history(3, 103, 2, "10")),
                new HistoricalKpiDataPreparer(),
                new HistoricalTrendAnalyzer(new ReportAnalyticsService()),
                provider,
                properties(),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private AnalyticalRuntimeResponse response(
            AnalyticalRuntimeRequest request,
            List<MaintenanceIntelligenceResult.AnomalyPoint> points
    ) {
        var anomaly = new MaintenanceIntelligenceResult.AnomalyAnalysis(
                MaintenanceIntelligenceResult.AnalysisState.COMPLETED, null, "SKLEARN_ISOLATION_FOREST",
                List.of("confirmed_value"), request.observations().size(), BigDecimal.ZERO,
                new MaintenanceIntelligenceResult.AnomalyScoreSemantics(
                        "MODEL_RELATIVE_EVIDENCE", "HIGHER_IS_MORE_ANOMALOUS", "FITTED_KPI_WINDOW",
                        false, false, false), points);
        var forecast = new MaintenanceIntelligenceResult.ForecastAnalysis(
                MaintenanceIntelligenceResult.AnalysisState.INSUFFICIENT_DATA,
                "FORECAST_MINIMUM_HISTORY_NOT_MET", null, Map.of(), request.observations().size(),
                request.configuration().forecastHorizon(), List.of(), 0, request.generatedAt(), List.of(), null,
                List.of(), null, null, null, "UNAVAILABLE", null);
        var latest = request.observations().getLast();
        var expectation = new MaintenanceIntelligenceResult.LatestObservationExpectation(
                MaintenanceIntelligenceResult.AnalysisState.INSUFFICIENT_DATA,
                "LATEST_EXPECTATION_MINIMUM_HISTORY_NOT_MET", latest.entryId(), latest.reportId(),
                latest.effectiveDate(), latest.value(), request.observations().size() - 1,
                null, null, null, false, null, null, Map.of(), null, null, null);
        return new AnalyticalRuntimeResponse(request.analysisId(), request.kpi().definitionId(), anomaly, forecast, expectation);
    }

    private ConfirmedKpiHistoryRecord history(long entryId, long reportId, int day, String value) {
        return new ConfirmedKpiHistoryRecord(KPI, entryId, reportId, START.plusDays(day), NOW.plusSeconds(entryId),
                ReportStatus.CONFIRMED, value == null ? null : new BigDecimal(value));
    }

    private MaintenanceIntelligenceProperties properties() {
        return new MaintenanceIntelligenceProperties("http://127.0.0.1:8092", Duration.ofSeconds(1),
                Duration.ofSeconds(5), 12, 200, 42, 5, 8, 7, 6, 3, 8, 7, 2, 0.95);
    }
}
