package com.factoryflow.intelligence.application;

import com.factoryflow.intelligence.domain.MaintenanceIntelligenceResult;
import com.factoryflow.intelligence.domain.MaintenanceIntelligenceResult.AnomalyPoint;
import com.factoryflow.intelligence.domain.PreparedKpiSeries;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaintenanceIntelligenceService {
    private final ConfirmedKpiHistorySource historySource;
    private final HistoricalKpiDataPreparer preparer;
    private final HistoricalTrendAnalyzer trendAnalyzer;
    private final MaintenanceIntelligenceProvider provider;
    private final MaintenanceIntelligenceProperties properties;
    private final Clock clock;

    public MaintenanceIntelligenceService(
            ConfirmedKpiHistorySource historySource,
            HistoricalKpiDataPreparer preparer,
            HistoricalTrendAnalyzer trendAnalyzer,
            MaintenanceIntelligenceProvider provider,
            MaintenanceIntelligenceProperties properties,
            Clock clock
    ) {
        this.historySource = historySource;
        this.preparer = preparer;
        this.trendAnalyzer = trendAnalyzer;
        this.provider = provider;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public MaintenanceIntelligenceResult analyze(Long kpiDefinitionId, LocalDate windowStart, LocalDate windowEnd) {
        return analyze(kpiDefinitionId, windowStart, windowEnd,
                new MaintenanceIntelligenceSettings(null, properties.forecastHorizon(), properties.seasonalPeriod()));
    }

    @Transactional(readOnly = true)
    public MaintenanceIntelligenceResult analyze(Long kpiDefinitionId, LocalDate windowStart, LocalDate windowEnd,
                                                 MaintenanceIntelligenceSettings settings) {
        PreparedKpiSeries series = preparer.prepare(kpiDefinitionId, windowStart, windowEnd,
                historySource.load(kpiDefinitionId, windowStart, windowEnd), settings.expectedCadenceDays());
        Instant generatedAt = clock.instant();
        AnalyticalRuntimeRequest request = request(series, generatedAt, settings);
        AnalyticalRuntimeResponse response = provider.analyze(request);
        validateResponse(request, response);
        return new MaintenanceIntelligenceResult(
                series.kpi(), windowStart, windowEnd, generatedAt,
                new MaintenanceIntelligenceResult.PreparationSummary(
                        series.sourceRecordCount(), series.usableObservationCount(), series.missingValueCount(), series.cadence()),
                series.observations(), trendAnalyzer.analyze(series), response.anomaly(), response.forecast(),
                response.latestObservationExpectation());
    }

    private AnalyticalRuntimeRequest request(PreparedKpiSeries series, Instant generatedAt,
                                             MaintenanceIntelligenceSettings settings) {
        return new AnalyticalRuntimeRequest(UUID.randomUUID().toString(), series.kpi(), series.windowStart(),
                series.windowEnd(), generatedAt, series.cadence(), series.observations(),
                new AnalyticalRuntimeRequest.Configuration(
                        properties.anomalyMinimumHistory(), properties.anomalyEstimators(),
                        properties.anomalyRandomState(), properties.anomalyRollingWindow(),
                        properties.forecastMinimumHistory(), settings.forecastHorizon(),
                        properties.backtestMinimumTraining(), properties.backtestMinimumFolds(),
                        properties.backtestMaximumFolds(),
                        settings.seasonalPeriod() == null ? properties.seasonalPeriod() : settings.seasonalPeriod(),
                        properties.seasonalMinimumCycles(),
                        properties.intervalConfidence()));
    }

    private void validateResponse(AnalyticalRuntimeRequest request, AnalyticalRuntimeResponse response) {
        if (response == null || !request.analysisId().equals(response.analysisId())
                || !request.kpi().definitionId().equals(response.kpiDefinitionId())
                || response.anomaly() == null || response.forecast() == null
                || response.latestObservationExpectation() == null) {
            throw new MaintenanceIntelligenceProviderException("Analytical runtime returned an invalid response identity");
        }
        Map<Long, PreparedKpiSeries.Observation> expected = new HashMap<>();
        request.observations().forEach(point -> expected.put(point.entryId(), point));
        validateAnomalySemantics(response);
        Set<Long> returnedEntryIds = new HashSet<>();
        for (AnomalyPoint point : response.anomaly().points()) {
            PreparedKpiSeries.Observation source = expected.get(point.entryId());
            if (source == null || !source.reportId().equals(point.reportId())
                    || !source.effectiveDate().equals(point.effectiveDate())
                    || point.value() == null || source.value().compareTo(point.value()) != 0
                    || !returnedEntryIds.add(point.entryId())) {
                throw new MaintenanceIntelligenceProviderException(
                        "Analytical runtime changed or invented anomaly observation identity");
            }
            if (response.anomaly().state() == MaintenanceIntelligenceResult.AnalysisState.COMPLETED
                    && (point.anomalyScore() == null || point.decisionFunction() == null
                    || point.anomalous() == null)) {
                throw new MaintenanceIntelligenceProviderException(
                        "Completed anomaly analysis omitted score evidence");
            }
        }
        if (returnedEntryIds.size() != expected.size()) {
            throw new MaintenanceIntelligenceProviderException("Anomaly analysis omitted source observations");
        }
        if (response.forecast().state() == MaintenanceIntelligenceResult.AnalysisState.COMPLETED) {
            validateCompletedForecast(request, response);
        }
        validateLatestExpectation(request, response);
    }

    private void validateLatestExpectation(AnalyticalRuntimeRequest request, AnalyticalRuntimeResponse response) {
        if (request.observations().isEmpty()) {
            return;
        }
        var expectedSource = request.observations().getLast();
        var expectation = response.latestObservationExpectation();
        if (!expectedSource.entryId().equals(expectation.entryId())
                || !expectedSource.reportId().equals(expectation.reportId())
                || !expectedSource.effectiveDate().equals(expectation.effectiveDate())
                || expectedSource.value().compareTo(expectation.actualValue()) != 0
                || expectation.trainingObservationCount() != request.observations().size() - 1) {
            throw new MaintenanceIntelligenceProviderException("Latest-observation expectation changed source identity");
        }
        if (expectation.state() == MaintenanceIntelligenceResult.AnalysisState.COMPLETED
                && (expectation.expectedValue() == null || expectation.selectedModelFamily() == null
                || (expectation.intervalAvailable() && (expectation.lowerBound() == null
                || expectation.upperBound() == null || expectation.outsideInterval() == null))
                || (!expectation.intervalAvailable() && (expectation.lowerBound() != null
                || expectation.upperBound() != null || expectation.outsideInterval() != null)))) {
            throw new MaintenanceIntelligenceProviderException("Latest-observation expectation is structurally invalid");
        }
    }

    private void validateAnomalySemantics(AnalyticalRuntimeResponse response) {
        var semantics = response.anomaly().scoreSemantics();
        if (semantics == null || !"MODEL_RELATIVE_EVIDENCE".equals(semantics.kind())
                || !"HIGHER_IS_MORE_ANOMALOUS".equals(semantics.orientation())
                || !"FITTED_KPI_WINDOW".equals(semantics.scope())
                || semantics.probability() || semantics.severity() || semantics.crossModelComparable()) {
            throw new MaintenanceIntelligenceProviderException("Analytical runtime changed anomaly score semantics");
        }
    }

    private void validateCompletedForecast(AnalyticalRuntimeRequest request, AnalyticalRuntimeResponse response) {
        var forecast = response.forecast();
        if (forecast.selectedModelFamily() == null || forecast.selectedMetrics() == null
                || forecast.points().size() != request.configuration().forecastHorizon()
                || forecast.requestedHorizon() != request.configuration().forecastHorizon()
                || forecast.rollingOriginCount() < request.configuration().backtestMinimumFolds()
                || forecast.modelSelection() == null || forecast.selectedModelDiagnostics() == null
                || request.observations().isEmpty()) {
            throw new MaintenanceIntelligenceProviderException("Completed forecast is structurally incomplete");
        }
        if (forecast.effectiveEvaluatedHorizons().size() != request.configuration().forecastHorizon()) {
            throw new MaintenanceIntelligenceProviderException("Forecast did not evaluate every requested horizon");
        }
        for (int step = 1; step <= request.configuration().forecastHorizon(); step++) {
            if (forecast.effectiveEvaluatedHorizons().get(step - 1) != step) {
                throw new MaintenanceIntelligenceProviderException("Forecast did not evaluate every requested horizon");
            }
        }
        var selection = forecast.modelSelection();
        if (selection.rawBest() == null || selection.parsimoniousChoice() == null || selection.selected() == null
                || !"sMAPE".equals(selection.primaryMetric())
                || !"ONE_STANDARD_ERROR_THEN_LOWEST_COMPLEXITY".equals(selection.rule())
                || !forecast.selectedModelFamily().equals(selection.selected().family())
                || !forecast.selectedModelConfiguration().equals(selection.selected().configuration())) {
            throw new MaintenanceIntelligenceProviderException("Forecast model-selection evidence is inconsistent");
        }
        boolean selectedCandidatePresent = forecast.candidates().stream().anyMatch(candidate ->
                candidate.state() == MaintenanceIntelligenceResult.CandidateState.EVALUATED
                        && forecast.selectedModelFamily().equals(candidate.family())
                        && forecast.selectedModelConfiguration().equals(candidate.configuration())
                        && candidate.rollingOriginCount() == forecast.rollingOriginCount()
                        && candidate.effectiveEvaluatedHorizons().equals(forecast.effectiveEvaluatedHorizons())
                        && candidate.perHorizonMetrics().size() == request.configuration().forecastHorizon());
        if (!selectedCandidatePresent) {
            throw new MaintenanceIntelligenceProviderException("Selected forecast candidate evidence is missing");
        }
        LocalDate previousDate = request.observations().getLast().effectiveDate();
        for (var point : forecast.points()) {
            if (point.effectiveDate() == null || !point.effectiveDate().isAfter(previousDate) || point.value() == null) {
                throw new MaintenanceIntelligenceProviderException("Forecast dates or values are invalid");
            }
            if (point.intervalAvailable()
                    && (point.lowerBound() == null || point.upperBound() == null
                    || point.lowerBound().compareTo(point.value()) > 0
                    || point.upperBound().compareTo(point.value()) < 0)) {
                throw new MaintenanceIntelligenceProviderException("Forecast interval does not contain its estimate");
            }
            if (!point.intervalAvailable() && (point.lowerBound() != null || point.upperBound() != null)) {
                throw new MaintenanceIntelligenceProviderException("Unavailable forecast interval contains bounds");
            }
            previousDate = point.effectiveDate();
        }
    }
}
