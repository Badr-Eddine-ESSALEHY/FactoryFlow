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
import java.util.concurrent.atomic.AtomicReference;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class PythonMaintenanceIntelligenceProviderTest {
    @Test
    void postsStructuredRequestToPrivateRuntimeAndDeserializesTypedResponse() throws Exception {
        String response = """
                {
                  "analysisId":"analysis-1",
                  "kpiDefinitionId":7,
                  "anomaly":{
                    "state":"INSUFFICIENT_DATA","insufficientReason":"ANOMALY_MINIMUM_HISTORY_NOT_MET",
                    "algorithm":"SKLEARN_ISOLATION_FOREST","featureNames":["confirmed_value"],
                    "trainingObservationCount":1,"anomalyThreshold":null,
                    "scoreSemantics":{"kind":"MODEL_RELATIVE_EVIDENCE","orientation":"HIGHER_IS_MORE_ANOMALOUS",
                      "scope":"FITTED_KPI_WINDOW","probability":false,"severity":false,"crossModelComparable":false},
                    "points":[{"entryId":1,"reportId":101,"effectiveDate":"2026-01-01",
                      "confirmedAt":"2026-01-01T12:00:00Z","value":0,"anomalyScore":null,
                      "decisionFunction":null,"anomalous":null}]
                  },
                  "forecast":{
                    "state":"INSUFFICIENT_DATA","insufficientReason":"FORECAST_MINIMUM_HISTORY_NOT_MET",
                    "selectedModelFamily":null,"selectedModelConfiguration":{},"trainingObservationCount":1,
                    "requestedHorizon":7,"effectiveEvaluatedHorizons":[],"rollingOriginCount":0,
                    "generatedAt":"2026-02-01T00:00:00Z","points":[],"selectedMetrics":null,
                    "candidates":[],"modelSelection":null,"selectedModelDiagnostics":null,
                    "selectionReason":null,"forecastDirection":"UNAVAILABLE",
                    "intervalConfidence":null
                  },
                  "latestObservationExpectation":{
                    "state":"INSUFFICIENT_DATA","insufficientReason":"LATEST_EXPECTATION_MINIMUM_HISTORY_NOT_MET",
                    "entryId":1,"reportId":101,"effectiveDate":"2026-01-01","actualValue":0,
                    "trainingObservationCount":0,"expectedValue":null,"lowerBound":null,"upperBound":null,
                    "intervalAvailable":false,"outsideInterval":null,"selectedModelFamily":null,
                    "selectedModelConfiguration":{},"selectedMetrics":null,"modelSelection":null,
                    "selectedModelDiagnostics":null
                  }
                }
                """;
        AtomicReference<String> receivedBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/analyze", exchange -> {
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        try {
            String runtimeUrl = "http://127.0.0.1:" + server.getAddress().getPort();
            var provider = new PythonMaintenanceIntelligenceProvider(
                    RestClient.builder(), properties(runtimeUrl), new ObjectMapper().findAndRegisterModules());
            var result = provider.analyze(request());

            assertThat(result.analysisId()).isEqualTo("analysis-1");
            assertThat(result.kpiDefinitionId()).isEqualTo(7L);
            assertThat(result.anomaly().insufficientReason()).isEqualTo("ANOMALY_MINIMUM_HISTORY_NOT_MET");
            assertThat(result.forecast().forecastDirection()).isEqualTo("UNAVAILABLE");
            assertThat(receivedBody.get()).contains("\"analysisId\":\"analysis-1\"", "\"value\":0");
        } finally {
            server.stop(0);
        }
    }

    private AnalyticalRuntimeRequest request() {
        var observation = new PreparedKpiSeries.Observation(1L, 101L, LocalDate.of(2026, 1, 1),
                Instant.parse("2026-01-01T12:00:00Z"), BigDecimal.ZERO);
        return new AnalyticalRuntimeRequest("analysis-1", new KpiIdentity(7L, "TEMP", "Température", "°C"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1), Instant.parse("2026-02-01T00:00:00Z"),
                new PreparedKpiSeries.CadenceMetadata(
                        PreparedKpiSeries.CadenceState.SINGLE_DATE, null, null,
                        PreparedKpiSeries.CadenceBasis.UNKNOWN,
                        PreparedKpiSeries.CadenceAmbiguity.INSUFFICIENT_OBSERVATIONS,
                        1, 0, 0, false, "NONE"),
                List.of(observation), new AnalyticalRuntimeRequest.Configuration(12, 200, 42, 5, 8, 7, 6, 3, 8, 7, 2, 0.95));
    }

    private MaintenanceIntelligenceProperties properties(String runtimeUrl) {
        return new MaintenanceIntelligenceProperties(runtimeUrl, Duration.ofSeconds(1),
                Duration.ofSeconds(5), 12, 200, 42, 5, 8, 7, 6, 3, 8, 7, 2, 0.95);
    }
}
