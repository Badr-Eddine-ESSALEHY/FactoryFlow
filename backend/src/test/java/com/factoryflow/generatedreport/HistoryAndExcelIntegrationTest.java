package com.factoryflow.generatedreport;

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
import com.factoryflow.generatedreport.persistence.GeneratedReportRepository;
import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.kpi.persistence.KpiDefinitionRepository;
import com.factoryflow.report.domain.AcquisitionSource;
import com.factoryflow.report.domain.MaintenanceReport;
import com.factoryflow.report.persistence.MaintenanceReportRepository;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
class HistoryAndExcelIntegrationTest {

    private static final LocalDate EFFECTIVE_DATE = LocalDate.of(2026, 8, 11);

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserAccountRepository users;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired KpiDefinitionRepository definitions;
    @Autowired MaintenanceReportRepository reports;
    @Autowired GeneratedReportRepository generatedReports;

    @Test
    void historyAndExcelUseConfirmedDataWhileExcludingDraftsAndPreservingMissingValues() throws Exception {
        String email = "excel-" + UUID.randomUUID() + "@example.com";
        UserAccount user = users.saveAndFlush(UserAccount.create(
                "Excel Engineer", email, passwordEncoder.encode("excel-password")
        ));
        KpiDefinition vrac = definitions.findAllByActiveOrderByDisplayNameAsc(true).stream()
                .filter(definition -> definition.getCode().equals("VRAC")).findFirst().orElseThrow();

        MaintenanceReport confirmedValue = report(user, vrac, true, new BigDecimal("15.8"), "confirmed value");
        MaintenanceReport confirmedMissing = report(user, vrac, true, null, "confirmed missing");
        MaintenanceReport draft = report(user, vrac, false, new BigDecimal("999"), "draft must be excluded");
        String token = login(email);

        mockMvc.perform(get("/api/reports")
                        .header("Authorization", "Bearer " + token)
                        .param("effectiveDate", EFFECTIVE_DATE.toString())
                        .param("status", "CONFIRMED")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"));

        mockMvc.perform(get("/api/reports/{id}", confirmedValue.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.entries[0].finalValue").value(15.8));
        mockMvc.perform(get("/api/reports/{id}", draft.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.entries[0].finalValue").doesNotExist());

        String generatedJson = mockMvc.perform(post("/api/generated-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type":"DAILY",
                                  "format":"EXCEL",
                                  "periodStart":"2026-08-11",
                                  "periodEnd":"2026-08-11"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generationStatus").value("READY"))
                .andExpect(jsonPath("$.format").value("EXCEL"))
                .andExpect(jsonPath("$.sourceReportIds.length()").value(2))
                .andReturn().getResponse().getContentAsString();

        JsonNode generated = objectMapper.readTree(generatedJson);
        long generatedId = generated.get("id").asLong();
        byte[] downloaded = mockMvc.perform(get("/api/generated-reports/{id}/file", generatedId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("FactoryFlow_DAILY_2026-08-11_v1.xlsx")))
                .andReturn().getResponse().getContentAsByteArray();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(downloaded))) {
            var sheet = workbook.getSheet("Maintenance KPIs");
            assertThat(sheet).isNotNull();
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue())
                    .isEqualTo("FactoryFlow Maintenance KPI Report");
            assertThat(sheet.getLastRowNum()).isEqualTo(7);
            assertThat(sheet.getRow(6).getCell(5).getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(sheet.getRow(6).getCell(5).getNumericCellValue()).isEqualTo(15.8);
            assertThat(sheet.getRow(7).getCell(5).getCellType()).isEqualTo(CellType.STRING);
            assertThat(sheet.getRow(7).getCell(5).getStringCellValue()).isEqualTo("Missing");
            sheet.forEach(row -> row.forEach(cell -> {
                if (cell.getCellType() == CellType.NUMERIC) {
                    assertThat(cell.getNumericCellValue()).isNotEqualTo(999.0);
                }
            }));
        }

        assertThat(generatedReports.findById(generatedId)).isPresent().get().satisfies(metadata -> {
            assertThat(metadata.getFilePath()).doesNotContain(":").doesNotStartWith("/");
            assertThat(metadata.getSourceReports()).extracting(MaintenanceReport::getId)
                    .containsExactlyInAnyOrder(confirmedValue.getId(), confirmedMissing.getId());
        });

        mockMvc.perform(get("/api/generated-reports")
                        .header("Authorization", "Bearer " + token)
                        .param("type", "DAILY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/generated-reports/{id}", generatedId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedBy").value(user.getId()));
    }

    private MaintenanceReport report(UserAccount user, KpiDefinition definition, boolean confirmed,
                                     BigDecimal value, String rawText) {
        MaintenanceReport report = MaintenanceReport.draft(user, EFFECTIVE_DATE, AcquisitionSource.PASTE, rawText);
        report.addEntry(definition, "Vrac", "Vrac: " + value, value, value, BigDecimal.ONE, false, "t", Set.of());
        if (confirmed) {
            report.getEntries().getFirst().confirm(value);
            report.confirm();
        }
        return reports.saveAndFlush(report);
    }

    private String login(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"excel-password"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }
}
