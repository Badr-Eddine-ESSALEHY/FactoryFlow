package com.factoryflow.intelligence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.factoryflow.analytics.domain.TrendDirection;
import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.auth.persistence.UserAccountRepository;
import com.factoryflow.intelligence.application.AlertNotificationStateService;
import com.factoryflow.intelligence.application.ContextualAlertPersistenceService;
import com.factoryflow.intelligence.application.MaintenanceIntelligencePersistenceService;
import com.factoryflow.intelligence.domain.ContextualAlertType;
import com.factoryflow.intelligence.domain.IntelligenceAnalysisStatus;
import com.factoryflow.intelligence.domain.KpiIdentity;
import com.factoryflow.intelligence.domain.KpiIntelligenceProfile;
import com.factoryflow.intelligence.domain.MaintenanceIntelligenceAnalysis;
import com.factoryflow.intelligence.domain.MaintenanceIntelligenceResult;
import com.factoryflow.intelligence.domain.PreparedKpiSeries;
import com.factoryflow.intelligence.persistence.KpiIntelligenceProfileRepository;
import com.factoryflow.intelligence.persistence.MaintenanceIntelligenceAlertRepository;
import com.factoryflow.intelligence.persistence.MaintenanceIntelligenceAnalysisRepository;
import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.kpi.persistence.KpiDefinitionRepository;
import com.factoryflow.notification.application.NotificationService;
import com.factoryflow.report.domain.AcquisitionSource;
import com.factoryflow.report.domain.KpiEntry;
import com.factoryflow.report.domain.MaintenanceReport;
import com.factoryflow.report.persistence.MaintenanceReportRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MaintenanceIntelligencePostgresIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired UserAccountRepository users;
    @Autowired KpiDefinitionRepository kpis;
    @Autowired MaintenanceReportRepository reports;
    @Autowired KpiIntelligenceProfileRepository profiles;
    @Autowired MaintenanceIntelligenceAnalysisRepository analyses;
    @Autowired MaintenanceIntelligenceAlertRepository alerts;
    @Autowired MaintenanceIntelligencePersistenceService persistence;
    @Autowired ContextualAlertPersistenceService contextualAlerts;
    @Autowired NotificationService notifications;
    @Autowired AlertNotificationStateService notificationStates;

    private Long userId;
    private Long kpiId;
    private Long reportId;

    @AfterEach
    void removeIntegrationFixture() {
        if (kpiId != null) {
            jdbc.update("DELETE FROM user_notifications WHERE related_intelligence_alert_id IN "
                    + "(SELECT id FROM maintenance_intelligence_alerts WHERE kpi_definition_id = ?)", kpiId);
        }
        if (userId != null) jdbc.update("DELETE FROM user_notifications WHERE user_id = ?", userId);
        if (kpiId == null) {
            if (userId != null) jdbc.update("DELETE FROM users WHERE id = ?", userId);
            return;
        }
        jdbc.update("DELETE FROM maintenance_intelligence_alerts WHERE kpi_definition_id = ?", kpiId);
        jdbc.update("DELETE FROM maintenance_intelligence_analyses WHERE kpi_definition_id = ?", kpiId);
        jdbc.update("DELETE FROM kpi_intelligence_profiles WHERE kpi_definition_id = ?", kpiId);
        if (reportId != null) {
            jdbc.update("DELETE FROM kpi_entry_warnings WHERE kpi_entry_id IN (SELECT id FROM kpi_entries WHERE report_id = ?)", reportId);
            jdbc.update("DELETE FROM report_unrecognized_lines WHERE report_id = ?", reportId);
            jdbc.update("DELETE FROM kpi_entries WHERE report_id = ?", reportId);
            jdbc.update("DELETE FROM maintenance_reports WHERE id = ?", reportId);
        }
        jdbc.update("DELETE FROM kpi_aliases WHERE kpi_definition_id = ?", kpiId);
        jdbc.update("DELETE FROM kpi_definitions WHERE id = ?", kpiId);
        if (userId != null) jdbc.update("DELETE FROM users WHERE id = ?", userId);
    }

    @Test
    void persistsAndServesVersionedIntelligenceEvidenceThroughPostgresql() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String email = "mi-postgres-" + suffix + "@example.com";
        UserAccount user = users.saveAndFlush(UserAccount.create(
                "MI Integration Engineer", email, passwordEncoder.encode("integration-password")));
        userId = user.getId();
        KpiDefinition kpi = kpis.saveAndFlush(KpiDefinition.create(
                "MI_IT_" + suffix.substring(0, 8), "MI PostgreSQL " + suffix.substring(0, 8),
                "Integration", "bar", null, null, true, List.of()));
        kpiId = kpi.getId();

        MaintenanceReport report = MaintenanceReport.draft(
                user, LocalDate.of(2026, 8, 31), AcquisitionSource.MANUAL, "MI PostgreSQL fixture");
        report.addEntry(kpi, "Pressure", "Pressure: 162", new BigDecimal("162"),
                new BigDecimal("162"), BigDecimal.ONE, false, "bar", Set.of());
        report = reports.saveAndFlush(report);
        report.getEntries().getFirst().confirm(new BigDecimal("162"));
        report.confirm();
        report = reports.saveAndFlush(report);
        reportId = report.getId();
        KpiEntry entry = report.getEntries().getFirst();
        String token = login(email);

        mockMvc.perform(get("/api/maintenance-intelligence/kpis/{kpiId}/profile", kpiId))
                .andExpect(status().isUnauthorized());

        String profileJson = mockMvc.perform(get("/api/maintenance-intelligence/kpis/{kpiId}/profile", kpiId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expectedCadenceDays").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long profileVersion = objectMapper.readTree(profileJson).get("version").asLong();

        mockMvc.perform(put("/api/maintenance-intelligence/kpis/{kpiId}/profile", kpiId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"enabled":true,"expectedCadenceDays":1,"forecastHorizon":7,
                                 "seasonalPeriod":7,"historyWindowDays":365,"version":%d}
                                """.formatted(profileVersion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expectedCadenceDays").value(1))
                .andExpect(jsonPath("$.seasonalPeriod").value(7));

        KpiIntelligenceProfile profile = profiles.findByKpiId(kpiId).orElseThrow();
        assertThatThrownBy(() -> profiles.saveAndFlush(KpiIntelligenceProfile.defaults(kpi)))
                .isInstanceOf(DataIntegrityViolationException.class);

        MaintenanceIntelligenceResult result = resultFor(kpi, report, entry);
        MaintenanceIntelligenceAnalysis successful = persistence.persistSuccess(
                profile, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), result,
                Instant.parse("2026-08-31T12:00:01Z"), 531L);

        MaintenanceIntelligenceAnalysis reloaded = analyses.findDetailedById(successful.getId()).orElseThrow();
        assertThat(reloaded.getProfileSnapshot().get("expectedCadenceDays").asInt()).isEqualTo(1);
        assertThat(objectMapper.treeToValue(reloaded.getResultSnapshot(), MaintenanceIntelligenceResult.class))
                .usingRecursiveComparison().isEqualTo(result);
        assertThat(reloaded.getLatestEntryId()).isEqualTo(entry.getId());
        assertThat(reloaded.getLatestReportId()).isEqualTo(reportId);

        var firstAlert = contextualAlerts.evaluateAndPersist(successful.getId());
        var repeatedAlert = contextualAlerts.evaluateAndPersist(successful.getId());
        assertThat(firstAlert.newlyCreated()).isTrue();
        assertThat(repeatedAlert.newlyCreated()).isFalse();
        assertThat(repeatedAlert.alertId()).isEqualTo(firstAlert.alertId());
        assertThat(alerts.count()).isGreaterThanOrEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM maintenance_intelligence_alerts WHERE source_entry_id = ? AND alert_type = ?",
                Integer.class, entry.getId(), ContextualAlertType.STRONG_CONTEXTUAL_DEVIATION.name())).isEqualTo(1);

        assertThat(notifications.notifyIntelligence(user, "Écart contextuel important",
                "Signal analytique à examiner.", reportId, firstAlert.alertId())).isTrue();
        assertThat(notifications.notifyIntelligence(user, "Écart contextuel important",
                "Signal analytique à examiner.", reportId, firstAlert.alertId())).isFalse();
        notificationStates.sent(firstAlert.alertId());
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM user_notifications WHERE related_intelligence_alert_id = ?",
                Integer.class, firstAlert.alertId())).isEqualTo(1);
        persistence.contextCompleted(successful.getId());

        MaintenanceIntelligenceAnalysis failed = persistence.persistFailure(
                profile, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 1),
                Instant.parse("2026-09-01T12:00:00Z"), 3000L,
                "ANALYTICAL_RUNTIME_FAILURE", "runtime unavailable");
        assertThat(analyses.findFirstByKpiIdOrderByGeneratedAtDesc(kpiId).orElseThrow().getId())
                .isEqualTo(failed.getId());
        assertThat(analyses.findFirstByKpiIdAndStatusInOrderByGeneratedAtDesc(kpiId,
                List.of(IntelligenceAnalysisStatus.COMPLETED, IntelligenceAnalysisStatus.INSUFFICIENT_DATA))
                .orElseThrow().getId()).isEqualTo(successful.getId());

        mockMvc.perform(get("/api/maintenance-intelligence/kpis/{kpiId}", kpiId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.id").value(successful.getId()))
                .andExpect(jsonPath("$.result.latestObservationExpectation.trainingObservationCount").value(12))
                .andExpect(jsonPath("$.result.latestObservationExpectation.outsideInterval").value(true));
        mockMvc.perform(get("/api/maintenance-intelligence/kpis/{kpiId}/analyses", kpiId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].status").value("TECHNICAL_FAILURE"));
        mockMvc.perform(get("/api/maintenance-intelligence/alerts/{id}", firstAlert.alertId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("STRONG_CONTEXTUAL_DEVIATION"))
                .andExpect(jsonPath("$.notificationStatus").value("SENT"));
        mockMvc.perform(get("/api/maintenance-intelligence/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpis[?(@.profile.kpiDefinitionId == %d)].latestSuccessfulAnalysis.id"
                        .formatted(kpiId)).value(org.hamcrest.Matchers.hasItem(successful.getId().intValue())))
                .andExpect(jsonPath("$.kpis[?(@.profile.kpiDefinitionId == %d)].latestRefreshAttempt.id"
                        .formatted(kpiId)).value(org.hamcrest.Matchers.hasItem(failed.getId().intValue())));
    }

    private String login(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"integration-password\"}".formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private MaintenanceIntelligenceResult resultFor(KpiDefinition kpi, MaintenanceReport report, KpiEntry entry) {
        Instant generatedAt = Instant.parse("2026-08-31T12:00:00Z");
        KpiIdentity identity = new KpiIdentity(kpi.getId(), kpi.getCode(), kpi.getDisplayName(), kpi.getUnit());
        PreparedKpiSeries.CadenceMetadata cadence = new PreparedKpiSeries.CadenceMetadata(
                PreparedKpiSeries.CadenceState.REGULAR, 1, 1,
                PreparedKpiSeries.CadenceBasis.CONFIGURED_EXPECTED,
                PreparedKpiSeries.CadenceAmbiguity.NONE, 13, 0, 0, false, "NONE");
        PreparedKpiSeries.Observation observation = new PreparedKpiSeries.Observation(
                entry.getId(), report.getId(), report.getEffectiveDate(), report.getConfirmedAt(), new BigDecimal("162"));
        MaintenanceIntelligenceResult.ForecastMetrics metrics = new MaintenanceIntelligenceResult.ForecastMetrics(
                new BigDecimal("2.1"), new BigDecimal("2.8"), new BigDecimal("1.9"),
                new BigDecimal("0.7"), new BigDecimal("0.6"));
        MaintenanceIntelligenceResult.ModelFitDiagnostics diagnostics =
                new MaintenanceIntelligenceResult.ModelFitDiagnostics(
                        true, true, true, true, true, new BigDecimal("0.21"), false, List.of());
        MaintenanceIntelligenceResult.ModelReference reference = new MaintenanceIntelligenceResult.ModelReference(
                "ETS", Map.of("variant", "SIMPLE_EXPONENTIAL_SMOOTHING"), metrics);
        MaintenanceIntelligenceResult.ModelSelectionDecision selection =
                new MaintenanceIntelligenceResult.ModelSelectionDecision(
                        reference, reference, reference, "SMAPE", new BigDecimal("0.2"),
                        new BigDecimal("2.1"), false, false, "ONE_STANDARD_ERROR_THEN_LOWEST_COMPLEXITY");
        return new MaintenanceIntelligenceResult(
                identity, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), generatedAt,
                new MaintenanceIntelligenceResult.PreparationSummary(13, 13, 0, cadence),
                List.of(observation),
                new MaintenanceIntelligenceResult.TrendAnalysis(
                        TrendDirection.INCREASING, new BigDecimal("0.4"), new BigDecimal("7"),
                        new BigDecimal("6.9"), 13),
                new MaintenanceIntelligenceResult.AnomalyAnalysis(
                        MaintenanceIntelligenceResult.AnalysisState.COMPLETED, null, "ISOLATION_FOREST",
                        List.of("value", "delta", "rollingDeviation"), 13, BigDecimal.ZERO,
                        new MaintenanceIntelligenceResult.AnomalyScoreSemantics(
                                "LOCAL_MODEL_SCORE", "LOWER_IS_MORE_ANOMALOUS", "PER_ANALYSIS", false, false, false),
                        List.of(new MaintenanceIntelligenceResult.AnomalyPoint(
                                entry.getId(), report.getId(), report.getEffectiveDate(), report.getConfirmedAt(),
                                new BigDecimal("162"), new BigDecimal("-0.24"), new BigDecimal("-0.09"), true))),
                new MaintenanceIntelligenceResult.ForecastAnalysis(
                        MaintenanceIntelligenceResult.AnalysisState.COMPLETED, null, "ETS",
                        Map.of("variant", "SIMPLE_EXPONENTIAL_SMOOTHING"), 13, 7,
                        List.of(1, 2, 3, 4, 5, 6, 7), 5, generatedAt,
                        List.of(new MaintenanceIntelligenceResult.ForecastPoint(
                                LocalDate.of(2026, 9, 1), new BigDecimal("109"),
                                new BigDecimal("100"), new BigDecimal("118"), true)),
                        metrics, List.of(), selection, diagnostics,
                        "ONE_STANDARD_ERROR_THEN_LOWEST_COMPLEXITY", "INCREASING", new BigDecimal("0.95")),
                new MaintenanceIntelligenceResult.LatestObservationExpectation(
                        MaintenanceIntelligenceResult.AnalysisState.COMPLETED, null,
                        entry.getId(), report.getId(), report.getEffectiveDate(), new BigDecimal("162"), 12,
                        new BigDecimal("108"), new BigDecimal("99"), new BigDecimal("117"), true, true,
                        "ETS", Map.of("variant", "SIMPLE_EXPONENTIAL_SMOOTHING"), metrics, selection, diagnostics));
    }
}
