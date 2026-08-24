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
import java.util.HashSet;
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

    private static final LocalDate EFFECTIVE_DATE = LocalDate.of(2099, 1, 1);

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserAccountRepository users;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired KpiDefinitionRepository definitions;
    @Autowired MaintenanceReportRepository reports;
    @Autowired GeneratedReportRepository generatedReports;

    @Test
    void individualExportIsIdScopedWhileDailyExcelConsolidatesConfirmedReportsOnly() throws Exception {
        String email = "excel-" + UUID.randomUUID() + "@example.com";
        UserAccount user = users.saveAndFlush(UserAccount.create(
                "Excel Engineer", email, passwordEncoder.encode("excel-password")
        ));
        KpiDefinition vrac = definitions.findAllByActiveOrderByDisplayNameAsc(true).stream()
                .filter(definition -> definition.getCode().equals("VRAC")).findFirst().orElseThrow();

        MaintenanceReport confirmedValue = report(user, vrac, true, new BigDecimal("15.8"), "confirmed value");
        MaintenanceReport confirmedMissing = report(user, vrac, true, null, "confirmed missing");
        MaintenanceReport confirmedOther = report(user, vrac, true, new BigDecimal("23.4"), "other same-day report");
        MaintenanceReport draft = report(user, vrac, false, new BigDecimal("999"), "draft must be excluded");
        String token = login(email);

        mockMvc.perform(get("/api/reports")
                        .header("Authorization", "Bearer " + token)
                        .param("effectiveDate", EFFECTIVE_DATE.toString())
                        .param("status", "CONFIRMED")
                        .param("submittedBy", user.getId().toString())
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
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

        String individualJson = mockMvc.perform(post("/api/generated-reports/individual")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reportId":%d,
                                  "format":"EXCEL"
                                }
                                """.formatted(confirmedValue.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("INDIVIDUAL"))
                .andExpect(jsonPath("$.sourceReportIds.length()").value(1))
                .andExpect(jsonPath("$.sourceReportIds[0]").value(confirmedValue.getId()))
                .andReturn().getResponse().getContentAsString();

        long individualId = objectMapper.readTree(individualJson).get("id").asLong();
        byte[] individualWorkbook = mockMvc.perform(get("/api/generated-reports/{id}/file", individualId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("_report-" + confirmedValue.getId())))
                .andReturn().getResponse().getContentAsByteArray();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(individualWorkbook))) {
            var sheet = workbook.getSheet("Rapport");
            int detailHeader = findRow(sheet, 0, "Date");
            assertThat(detailHeader).isGreaterThanOrEqualTo(0);
            assertThat(sheet.getRow(detailHeader + 1).getCell(2).getNumericCellValue()).isEqualTo(15.8);
            assertThat(sheet.getRow(detailHeader + 3).getCell(0).getStringCellValue())
                    .isEqualTo("QUALITÉ DES DONNÉES");
        }

        mockMvc.perform(post("/api/generated-reports/individual")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reportId":%d,"format":"PDF"}
                                """.formatted(draft.getId())))
                .andExpect(status().isConflict());

        String generatedJson = mockMvc.perform(post("/api/generated-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type":"DAILY",
                                  "format":"EXCEL",
                                  "periodStart":"2099-01-01",
                                  "periodEnd":"2099-01-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generationStatus").value("READY"))
                .andExpect(jsonPath("$.format").value("EXCEL"))
                .andExpect(jsonPath("$.sourceReportIds.length()").value(3))
                .andReturn().getResponse().getContentAsString();

        JsonNode generated = objectMapper.readTree(generatedJson);
        long generatedId = generated.get("id").asLong();
        byte[] downloaded = mockMvc.perform(get("/api/generated-reports/{id}/file", generatedId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("FactoryFlow_DAILY_2099-01-01_v1.xlsx")))
                .andReturn().getResponse().getContentAsByteArray();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(downloaded))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
            var reportSheet = workbook.getSheet("Rapport");
            assertThat(reportSheet).isNotNull();
            assertThat(reportSheet.getDrawingPatriarch().getShapes()).hasSize(1);
            assertThat(reportSheet.getDrawingPatriarch().getCharts()).isEmpty();
            assertThat(workbook.getAllPictures()).hasSize(1);
            try (var officialLogo = getClass().getResourceAsStream("/reporting/alf-mabrouk-logo.png")) {
                assertThat(officialLogo).isNotNull();
                byte[] expectedLogo = officialLogo.readAllBytes();
                assertThat(workbook.getAllPictures()).allSatisfy(picture ->
                        assertThat(picture.getData()).isEqualTo(expectedLogo));
            }
            int detailHeader = findRow(reportSheet, 0, "Date");
            assertThat(detailHeader).isGreaterThanOrEqualTo(0);
            assertThat(reportSheet.getRow(detailHeader).getLastCellNum()).isEqualTo((short) 5);
            assertThat(reportSheet.getRow(detailHeader + 1).getCell(2).getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(reportSheet.getRow(detailHeader + 1).getCell(2).getNumericCellValue()).isEqualTo(15.8);
            assertThat(reportSheet.getRow(detailHeader + 2).getCell(2).getCellType()).isEqualTo(CellType.BLANK);
            assertThat(reportSheet.getRow(detailHeader + 3).getCell(2).getNumericCellValue()).isEqualTo(23.4);
            reportSheet.forEach(row -> row.forEach(cell -> {
                if (cell.getCellType() == CellType.NUMERIC) {
                    assertThat(cell.getNumericCellValue()).isNotEqualTo(999.0);
                }
                if (cell.getCellType() == CellType.STRING) {
                    assertThat(cell.getStringCellValue()).doesNotContain(
                            "PASTE", "CONFIRMED", "Contrôle de cohérence Excel");
                }
            }));
        }

        assertThat(generatedReports.findById(generatedId)).isPresent().get().satisfies(metadata -> {
            assertThat(metadata.getFilePath()).doesNotContain(":").doesNotStartWith("/");
            assertThat(metadata.getSourceReports()).extracting(MaintenanceReport::getId)
                    .containsExactlyInAnyOrder(
                            confirmedValue.getId(), confirmedMissing.getId(), confirmedOther.getId());
        });

        mockMvc.perform(get("/api/generated-reports")
                        .header("Authorization", "Bearer " + token)
                        .param("type", "DAILY")
                        .param("dateFrom", EFFECTIVE_DATE.toString())
                        .param("dateTo", EFFECTIVE_DATE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/generated-reports/{id}", generatedId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedBy").value(user.getId()));
    }

    @Test
    void weeklyMonthlyAndCustomPeriodsRespectInclusiveCalendarBoundaries() throws Exception {
        String email = "periods-" + UUID.randomUUID() + "@example.com";
        UserAccount user = users.saveAndFlush(UserAccount.create(
                "Period Engineer", email, passwordEncoder.encode("excel-password")
        ));
        KpiDefinition vrac = definitions.findAllByActiveOrderByDisplayNameAsc(true).stream()
                .filter(definition -> definition.getCode().equals("VRAC")).findFirst().orElseThrow();
        MaintenanceReport january = report(user, vrac, LocalDate.of(2099, 1, 31), true,
                new BigDecimal("1"), "January boundary");
        MaintenanceReport februaryStart = report(user, vrac, LocalDate.of(2099, 2, 1), true,
                new BigDecimal("2"), "February start");
        MaintenanceReport weekly = report(user, vrac, LocalDate.of(2099, 2, 4), true,
                new BigDecimal("3"), "Weekly middle");
        MaintenanceReport februaryEnd = report(user, vrac, LocalDate.of(2099, 2, 28), true,
                new BigDecimal("4"), "February end");
        report(user, vrac, LocalDate.of(2099, 3, 1), true, new BigDecimal("5"), "March boundary");
        report(user, vrac, LocalDate.of(2099, 2, 4), false, new BigDecimal("999"), "Draft excluded");
        String token = login(email);

        assertThat(generateSourceIds(token, "WEEKLY", "2099-02-02", "2099-02-08"))
                .containsExactly(weekly.getId());
        assertThat(generateSourceIds(token, "MONTHLY", "2099-02-01", "2099-02-28"))
                .containsExactlyInAnyOrder(februaryStart.getId(), weekly.getId(), februaryEnd.getId());
        assertThat(generateSourceIds(token, "CUSTOM", "2099-01-31", "2099-02-01"))
                .containsExactlyInAnyOrder(january.getId(), februaryStart.getId());
    }

    private Set<Long> generateSourceIds(String token, String type, String start, String end) throws Exception {
        String response = mockMvc.perform(post("/api/generated-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"%s","format":"PDF","periodStart":"%s","periodEnd":"%s"}
                                """.formatted(type, start, end)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value(type))
                .andReturn().getResponse().getContentAsString();
        Set<Long> ids = new HashSet<>();
        objectMapper.readTree(response).get("sourceReportIds").forEach(node -> ids.add(node.asLong()));
        return ids;
    }

    private int findRow(org.apache.poi.xssf.usermodel.XSSFSheet sheet, int column, String expected) {
        for (int index = 0; index <= sheet.getLastRowNum(); index++) {
            var row = sheet.getRow(index);
            if (row == null || row.getCell(column) == null) {
                continue;
            }
            var cell = row.getCell(column);
            if (cell.getCellType() == CellType.STRING && expected.equals(cell.getStringCellValue())) {
                return index;
            }
        }
        return -1;
    }

    private MaintenanceReport report(UserAccount user, KpiDefinition definition, boolean confirmed,
                                     BigDecimal value, String rawText) {
        return report(user, definition, EFFECTIVE_DATE, confirmed, value, rawText);
    }

    private MaintenanceReport report(UserAccount user, KpiDefinition definition, LocalDate effectiveDate,
                                     boolean confirmed, BigDecimal value, String rawText) {
        MaintenanceReport report = MaintenanceReport.draft(user, effectiveDate, AcquisitionSource.PASTE, rawText);
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
