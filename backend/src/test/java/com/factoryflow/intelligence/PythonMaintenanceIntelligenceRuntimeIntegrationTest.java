package com.factoryflow.intelligence;

import com.fasterxml.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;

import com.factoryflow.intelligence.application.AnalyticalRuntimeRequest;
import com.factoryflow.intelligence.application.MaintenanceIntelligenceProperties;
import com.factoryflow.intelligence.domain.KpiIdentity;
import com.factoryflow.intelligence.domain.PreparedKpiSeries;
import com.factoryflow.intelligence.infrastructure.runtime.PythonMaintenanceIntelligenceProvider;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.web.client.RestClient;

@EnabledIfEnvironmentVariable(named = "FACTORYFLOW_INTELLIGENCE_INTEGRATION", matches = "true")
class PythonMaintenanceIntelligenceRuntimeIntegrationTest {
    @Test
    void exchangesTheProductionJsonContractWithTheRunningPythonRuntime() {
        var properties = new MaintenanceIntelligenceProperties("http://127.0.0.1:8092", Duration.ofSeconds(1),
                Duration.ofSeconds(120), 12, 200, 42, 5, 8, 4, 6, 3, 8, 7, 2, 0.95);
        var provider = new PythonMaintenanceIntelligenceProvider(
                RestClient.builder(), properties, new ObjectMapper().findAndRegisterModules());
        LocalDate start = LocalDate.of(2026, 1, 1);
        List<PreparedKpiSeries.Observation> observations = IntStream.range(0, 14)
                .mapToObj(index -> new PreparedKpiSeries.Observation((long) index + 1, (long) index + 101,
                        start.plusDays(index), Instant.parse("2026-01-01T12:00:00Z").plusSeconds(index),
                        BigDecimal.valueOf(index == 13 ? 40 : 10 + index % 3)))
                .toList();
        var request = new AnalyticalRuntimeRequest("java-python-contract", new KpiIdentity(7L, "TEMP", "Température", "°C"),
                start, start.plusDays(13), Instant.parse("2026-02-01T00:00:00Z"),
                new PreparedKpiSeries.CadenceMetadata(
                        PreparedKpiSeries.CadenceState.REGULAR, 1, null,
                        PreparedKpiSeries.CadenceBasis.INFERRED_OBSERVED,
                        PreparedKpiSeries.CadenceAmbiguity.NONE,
                        14, 0, 0, false, "NONE"),
                observations, new AnalyticalRuntimeRequest.Configuration(12, 200, 42, 5, 8, 4, 6, 3, 8, 7, 2, 0.95));

        var response = provider.analyze(request);

        assertThat(response.analysisId()).isEqualTo("java-python-contract");
        assertThat(response.kpiDefinitionId()).isEqualTo(7L);
        assertThat(response.anomaly().points()).hasSize(14);
        assertThat(response.forecast().points()).hasSize(4);
        assertThat(response.forecast().candidates()).isNotEmpty();
        assertThat(response.forecast().requestedHorizon()).isEqualTo(4);
        assertThat(response.forecast().effectiveEvaluatedHorizons()).containsExactly(1, 2, 3, 4);
        assertThat(response.forecast().rollingOriginCount()).isGreaterThanOrEqualTo(3);
        assertThat(response.latestObservationExpectation().entryId()).isEqualTo(14L);
        assertThat(response.latestObservationExpectation().trainingObservationCount()).isEqualTo(13);
    }
}
