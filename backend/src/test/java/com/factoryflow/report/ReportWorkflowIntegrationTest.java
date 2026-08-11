package com.factoryflow.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.auth.persistence.UserAccountRepository;
import com.factoryflow.kpi.persistence.KpiDefinitionRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReportWorkflowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserAccountRepository users;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired KpiDefinitionRepository definitions;
    @Autowired JdbcTemplate jdbc;

    @Test
    void authenticatedAnalyzeDraftReviewAndConfirmFlowPersistsTraceableValues() throws Exception {
        String email = "flow-" + UUID.randomUUID() + "@example.com";
        users.saveAndFlush(UserAccount.create("Flow Engineer", email, passwordEncoder.encode("flow-password")));
        String token = login(email);
        Long vracId = definitions.findAllByActiveOrderByDisplayNameAsc(true).stream()
                .filter(definition -> definition.getCode().equals("VRAC"))
                .findFirst().orElseThrow().getId();

        Integer reportsBeforeAnalysis = jdbc.queryForObject("SELECT COUNT(*) FROM maintenance_reports", Integer.class);
        mockMvc.perform(post("/api/reports/analyze")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rawText":"Vrac : 15,8\\nUnknown Label 123","source":"PASTE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recognizedCount").value(1))
                .andExpect(jsonPath("$.unrecognizedCount").value(1))
                .andExpect(jsonPath("$.entries[0].extractedValue").value(15.8));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM maintenance_reports", Integer.class))
                .isEqualTo(reportsBeforeAnalysis);

        String draftBody = mockMvc.perform(post("/api/reports/drafts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "effectiveDate":"2026-08-11",
                                  "source":"PASTE",
                                  "rawText":"Vrac : 15,8\\nUnknown Label 123",
                                  "entries":[{
                                    "kpiDefinitionId":%d,
                                    "sourceLabel":"Vrac",
                                    "sourceLine":"Vrac : 15,8",
                                    "extractedValue":15.8,
                                    "currentValue":16.1,
                                    "confidenceScore":1.0,
                                    "editedByUser":true,
                                    "capturedUnit":"t",
                                    "warnings":[]
                                  }],
                                  "unrecognizedLines":[{
                                    "sourceLine":"Unknown Label 123",
                                    "resolution":"UNRESOLVED"
                                  }]
                                }
                                """.formatted(vracId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.entries[0].extractedValue").value(15.8))
                .andExpect(jsonPath("$.entries[0].currentValue").value(16.1))
                .andReturn().getResponse().getContentAsString();

        JsonNode draft = objectMapper.readTree(draftBody);
        long reportId = draft.get("id").asLong();
        long unknownLineId = draft.get("unrecognizedLines").get(0).get("id").asLong();

        mockMvc.perform(post("/api/reports/{id}/confirm", reportId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "entries":[{"kpiDefinitionId":%d,"finalValue":16.1}],
                                  "unrecognizedLineResolutions":[{"lineId":%d,"resolution":"IGNORED"}]
                                }
                                """.formatted(vracId, unknownLineId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.entries[0].extractedValue").value(15.8))
                .andExpect(jsonPath("$.entries[0].finalValue").value(16.1))
                .andExpect(jsonPath("$.entries[0].editedByUser").value(true))
                .andExpect(jsonPath("$.unrecognizedLines[0].resolution").value("IGNORED"));

        assertThat(jdbc.queryForObject(
                "SELECT status FROM maintenance_reports WHERE id = ?", String.class, reportId
        )).isEqualTo("CONFIRMED");
        assertThat(jdbc.queryForObject(
                "SELECT extracted_value FROM kpi_entries WHERE report_id = ?", BigDecimal.class, reportId
        )).isEqualByComparingTo("15.8");
        assertThat(jdbc.queryForObject(
                "SELECT final_value FROM kpi_entries WHERE report_id = ?", BigDecimal.class, reportId
        )).isEqualByComparingTo("16.1");
        assertThat(jdbc.queryForObject(
                "SELECT resolution_status FROM report_unrecognized_lines WHERE report_id = ?", String.class, reportId
        )).isEqualTo("IGNORED");
    }

    private String login(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"flow-password"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }
}
