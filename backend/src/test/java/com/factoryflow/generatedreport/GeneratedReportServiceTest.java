package com.factoryflow.generatedreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.factoryflow.analytics.application.ReportAnalyticsService;
import com.factoryflow.auth.application.AuthenticationService;
import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.generatedreport.api.IndividualReportExportRequest;
import com.factoryflow.generatedreport.application.ExcelReportGenerator;
import com.factoryflow.generatedreport.application.GeneratedReportService;
import com.factoryflow.generatedreport.application.PdfReportGenerator;
import com.factoryflow.generatedreport.application.ReportGenerationData;
import com.factoryflow.generatedreport.domain.GeneratedReport;
import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.generatedreport.persistence.GeneratedReportRepository;
import com.factoryflow.generatedreport.storage.ReportStorageService;
import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.report.domain.AcquisitionSource;
import com.factoryflow.report.domain.MaintenanceReport;
import com.factoryflow.report.persistence.MaintenanceReportRepository;
import com.factoryflow.shared.error.ApiErrorCode;
import com.factoryflow.shared.error.ApiException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class GeneratedReportServiceTest {

    @Test
    void individualExportUsesExactConfirmedIdWithoutDateScopeQuery() {
        Fixture fixture = new Fixture();
        MaintenanceReport selected = fixture.report(23L, true);
        when(fixture.maintenanceReports.findById(23L)).thenReturn(Optional.of(selected));

        var response = fixture.service.generateIndividual(
                fixture.user.getEmail(),
                new IndividualReportExportRequest(23L, GeneratedReportFormat.PDF)
        );

        assertThat(response.type().name()).isEqualTo("INDIVIDUAL");
        assertThat(response.sourceReportIds()).containsExactly(23L);
        assertThat(response.fileName()).contains("_report-23_");
        verify(fixture.maintenanceReports, never())
                .findAllByStatusAndEffectiveDateBetweenOrderByEffectiveDateAscIdAsc(any(), any(), any());
        ArgumentCaptor<ReportGenerationData> data = ArgumentCaptor.forClass(ReportGenerationData.class);
        verify(fixture.pdf).generate(data.capture());
        assertThat(data.getValue().rows()).hasSize(1)
                .extracting(ReportGenerationData.Row::sourceReportId)
                .containsExactly(23L);
    }

    @Test
    void individualExportRejectsDraftBeforeRenderingOrStorage() {
        Fixture fixture = new Fixture();
        when(fixture.maintenanceReports.findById(24L))
                .thenReturn(Optional.of(fixture.report(24L, false)));

        assertThatThrownBy(() -> fixture.service.generateIndividual(
                fixture.user.getEmail(),
                new IndividualReportExportRequest(24L, GeneratedReportFormat.EXCEL)
        )).isInstanceOfSatisfying(ApiException.class, error -> {
            assertThat(error.getCode()).isEqualTo(ApiErrorCode.REPORT_INVALID_STATE);
            assertThat(error.getStatus().value()).isEqualTo(409);
        });

        verify(fixture.excel, never()).generate(any());
        verify(fixture.storage, never()).store(any(), any());
    }

    private static final class Fixture {
        private final GeneratedReportRepository generatedReports = mock(GeneratedReportRepository.class);
        private final MaintenanceReportRepository maintenanceReports = mock(MaintenanceReportRepository.class);
        private final AuthenticationService authentication = mock(AuthenticationService.class);
        private final ExcelReportGenerator excel = mock(ExcelReportGenerator.class);
        private final PdfReportGenerator pdf = mock(PdfReportGenerator.class);
        private final ReportStorageService storage = mock(ReportStorageService.class);
        private final UserAccount user = UserAccount.create("Nadia", "nadia@example.com", "hash");
        private final KpiDefinition kpi = KpiDefinition.create(
                "VRAC", "Vrac", "Production", "t", null, null, true, List.of());
        private final GeneratedReportService service;

        private Fixture() {
            ReflectionTestUtils.setField(user, "id", 1L);
            ReflectionTestUtils.setField(kpi, "id", 10L);
            when(authentication.requireUser(user.getEmail())).thenReturn(user);
            when(pdf.generate(any())).thenReturn("pdf".getBytes());
            when(excel.generate(any())).thenReturn("excel".getBytes());
            when(storage.store(any(), any())).thenReturn("stored/report");
            when(generatedReports
                    .findFirstByTypeAndFormatAndPeriodStartAndPeriodEndAndOriginAndSourceReports_IdOrderByVersionDesc(
                            any(), any(), any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(generatedReports.saveAndFlush(any())).thenAnswer(invocation -> {
                GeneratedReport report = invocation.getArgument(0);
                ReflectionTestUtils.setField(report, "id", 99L);
                return report;
            });
            service = new GeneratedReportService(
                    generatedReports,
                    maintenanceReports,
                    authentication,
                    excel,
                    pdf,
                    storage,
                    Clock.fixed(Instant.parse("2026-08-22T15:00:00Z"), ZoneOffset.UTC),
                    new ReportAnalyticsService()
            );
        }

        private MaintenanceReport report(Long id, boolean confirmed) {
            MaintenanceReport report = MaintenanceReport.draft(
                    user,
                    LocalDate.of(2026, 8, 22),
                    AcquisitionSource.MANUAL,
                    "Vrac: 12.5"
            );
            report.addEntry(
                    kpi,
                    "Vrac",
                    "Vrac: 12.5",
                    new BigDecimal("12.5"),
                    new BigDecimal("12.5"),
                    BigDecimal.ONE,
                    false,
                    "t",
                    Set.of()
            );
            ReflectionTestUtils.setField(report, "id", id);
            if (confirmed) {
                report.getEntries().getFirst().confirm(new BigDecimal("12.5"));
                report.confirm();
            }
            return report;
        }
    }
}
