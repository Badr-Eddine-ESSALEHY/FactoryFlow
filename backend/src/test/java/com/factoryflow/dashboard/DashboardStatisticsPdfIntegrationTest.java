package com.factoryflow.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.auth.persistence.UserAccountRepository;
import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.kpi.persistence.KpiDefinitionRepository;
import com.factoryflow.report.domain.AcquisitionSource;
import com.factoryflow.report.domain.MaintenanceReport;
import com.factoryflow.report.persistence.MaintenanceReportRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DashboardStatisticsPdfIntegrationTest {
    @Autowired MockMvc mockMvc; @Autowired ObjectMapper json; @Autowired UserAccountRepository users;
    @Autowired PasswordEncoder passwords; @Autowired KpiDefinitionRepository definitions;
    @Autowired MaintenanceReportRepository reports;

    @Test
    void confirmedDataDrivesDashboardStatisticsAndAReopenablePdf() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.of("Africa/Casablanca"));
        String suffix = UUID.randomUUID().toString();
        UserAccount user = users.saveAndFlush(UserAccount.create("Dashboard Engineer", "dashboard-" + suffix + "@example.com",
                passwords.encode("dashboard-password")));
        KpiDefinition kpi = definitions.saveAndFlush(KpiDefinition.create("TEST_" + suffix.replace("-", "").substring(0, 12),
                "Test pressure " + suffix.substring(0, 6), "Test", null, null, null, true, List.of()));
        save(user, kpi, today, true, new BigDecimal("15.8"));
        save(user, kpi, today, true, null);
        save(user, kpi, today, false, new BigDecimal("999"));
        String token = login(user.getEmail());

        mockMvc.perform(get("/api/dashboard").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayConfirmedReportCount").isNumber())
                .andExpect(jsonPath("$.todayDraftOrPendingReportCount").isNumber())
                .andExpect(jsonPath("$.latestKpis[?(@.kpiDefinitionId == %s)].value".formatted(kpi.getId())).value(15.8));

        mockMvc.perform(get("/api/statistics").header("Authorization", "Bearer " + token)
                        .param("kpiDefinitionId", kpi.getId().toString())
                        .param("dateFrom", today.toString()).param("dateTo", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpis[0].sampleCount").value(1))
                .andExpect(jsonPath("$.kpis[0].missingValueCount").value(1))
                .andExpect(jsonPath("$.kpis[0].average").value(15.8))
                .andExpect(jsonPath("$.kpis[0].maximum").value(15.8));

        String body = mockMvc.perform(post("/api/generated-reports").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                          {"type":"DAILY","format":"PDF","periodStart":"%s","periodEnd":"%s"}
                          """.formatted(today, today)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.format").value("PDF"))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(body).get("id").asLong();
        byte[] pdf = mockMvc.perform(get("/api/generated-reports/{id}/file", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(header().string("Content-Type", "application/pdf"))
                .andReturn().getResponse().getContentAsByteArray();
        try (var document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);
            assertThat(document.getPage(0).getResources().getXObjectNames()).isNotEmpty();
            assertThat(text).contains("RAPPORT JOURNALIER DE MAINTENANCE", "15,8", "Non renseigné",
                            "RAPPORTS SOURCES", "SOUMIS PAR", "CONFIRMÉ LE", "PÉRIODE", "GÉNÉRÉ LE", "—")
                    .doesNotContain("15.8")
                    .doesNotContain("999", "Missing", "PASTE", "Local Excel Verification");
        }
    }

    private void save(UserAccount user, KpiDefinition kpi, LocalDate date, boolean confirmed, BigDecimal value) {
        MaintenanceReport report = MaintenanceReport.draft(user, date, AcquisitionSource.MANUAL, "test");
        report.addEntry(kpi, kpi.getDisplayName(), "test", value, value, BigDecimal.ONE, false, kpi.getUnit(), Set.of());
        if (confirmed) { report.getEntries().getFirst().confirm(value); report.confirm(); }
        reports.saveAndFlush(report);
    }
    private String login(String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"dashboard-password\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("accessToken").asText();
    }
}
