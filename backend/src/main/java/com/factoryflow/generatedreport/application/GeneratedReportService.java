package com.factoryflow.generatedreport.application;

import com.factoryflow.auth.application.AuthenticationService;
import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.generatedreport.api.GenerateReportRequest;
import com.factoryflow.generatedreport.api.GeneratedReportResponse;
import com.factoryflow.generatedreport.domain.GeneratedReport;
import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.generatedreport.domain.GeneratedReportType;
import com.factoryflow.generatedreport.domain.GenerationStatus;
import com.factoryflow.generatedreport.domain.ReportPeriod;
import com.factoryflow.generatedreport.persistence.GeneratedReportRepository;
import com.factoryflow.generatedreport.storage.ReportStorageService;
import com.factoryflow.generatedreport.storage.StoredReportFile;
import com.factoryflow.report.domain.MaintenanceReport;
import com.factoryflow.report.domain.ReportStatus;
import com.factoryflow.report.persistence.MaintenanceReportRepository;
import com.factoryflow.shared.api.PageResponse;
import com.factoryflow.shared.error.ApiErrorCode;
import com.factoryflow.shared.error.ApiException;
import jakarta.persistence.criteria.Predicate;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GeneratedReportService {

    private final GeneratedReportRepository generatedReports;
    private final MaintenanceReportRepository maintenanceReports;
    private final AuthenticationService authentication;
    private final ExcelReportGenerator excelGenerator;
    private final ReportStorageService storage;
    private final Clock clock;

    public GeneratedReportService(GeneratedReportRepository generatedReports,
                                  MaintenanceReportRepository maintenanceReports,
                                  AuthenticationService authentication,
                                  ExcelReportGenerator excelGenerator,
                                  ReportStorageService storage,
                                  Clock clock) {
        this.generatedReports = generatedReports;
        this.maintenanceReports = maintenanceReports;
        this.authentication = authentication;
        this.excelGenerator = excelGenerator;
        this.storage = storage;
        this.clock = clock;
    }

    @Transactional
    public GeneratedReportResponse generate(String email, GenerateReportRequest request) {
        UserAccount user = authentication.requireUser(email);
        ReportPeriod period;
        try {
            period = ReportPeriod.validated(request.type(), request.periodStart(), request.periodEnd());
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, exception.getMessage());
        }
        List<MaintenanceReport> sourceReports = maintenanceReports
                .findAllByStatusAndEffectiveDateBetweenOrderByEffectiveDateAscIdAsc(
                        ReportStatus.CONFIRMED, period.start(), period.end());
        GeneratedReport previous = generatedReports
                .findFirstByTypeAndFormatAndPeriodStartAndPeriodEndOrderByVersionDesc(
                        request.type(), request.format(), period.start(), period.end())
                .orElse(null);
        int version = previous == null ? 1 : previous.getVersion() + 1;
        Instant generatedAt = clock.instant();
        String fileName = fileName(request.type(), period, version);
        ExcelReportData data = toExcelData(request.type(), period, generatedAt, sourceReports);
        byte[] workbook;
        try {
            workbook = excelGenerator.generate(data);
        } catch (RuntimeException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.REPORT_GENERATION_FAILED,
                    "The Excel report could not be generated.");
        }
        String reference = storage.store(fileName, workbook);
        try {
            GeneratedReport report = GeneratedReport.ready(
                    request.type(), GeneratedReportFormat.EXCEL, period, generatedAt, reference, fileName, user,
                    version, previous, new LinkedHashSet<>(sourceReports)
            );
            return GeneratedReportResponse.from(generatedReports.saveAndFlush(report));
        } catch (RuntimeException exception) {
            storage.delete(reference);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<GeneratedReportResponse> list(
            GeneratedReportType type,
            GeneratedReportFormat format,
            GenerationStatus status,
            LocalDate dateFrom,
            LocalDate dateTo,
            Pageable pageable
    ) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR,
                    "dateFrom must be on or before dateTo.");
        }
        Specification<GeneratedReport> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (type != null) predicates.add(builder.equal(root.get("type"), type));
            if (format != null) predicates.add(builder.equal(root.get("format"), format));
            if (status != null) predicates.add(builder.equal(root.get("generationStatus"), status));
            if (dateFrom != null) predicates.add(builder.greaterThanOrEqualTo(root.get("periodEnd"), dateFrom));
            if (dateTo != null) predicates.add(builder.lessThanOrEqualTo(root.get("periodStart"), dateTo));
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        return PageResponse.from(generatedReports.findAll(specification, pageable), GeneratedReportResponse::from);
    }

    @Transactional(readOnly = true)
    public GeneratedReportResponse get(Long id) {
        return GeneratedReportResponse.from(requireReport(id));
    }

    @Transactional(readOnly = true)
    public DownloadedGeneratedReport download(Long id) {
        GeneratedReport report = requireReport(id);
        if (report.getGenerationStatus() != GenerationStatus.READY) {
            throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.REPORT_INVALID_STATE,
                    "Only ready generated reports can be downloaded.");
        }
        StoredReportFile file = storage.read(report.getFilePath());
        return new DownloadedGeneratedReport(report.getFileName(), file);
    }

    private GeneratedReport requireReport(Long id) {
        return generatedReports.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.GENERATED_REPORT_NOT_FOUND,
                        "Generated report not found."));
    }

    private ExcelReportData toExcelData(GeneratedReportType type, ReportPeriod period, Instant generatedAt,
                                        List<MaintenanceReport> reports) {
        List<ExcelReportData.Row> rows = reports.stream().flatMap(report -> report.getEntries().stream().map(entry ->
                new ExcelReportData.Row(
                        report.getEffectiveDate(), report.getId(), report.getSource(),
                        entry.getDefinition().getDisplayName(), entry.getDefinition().getUnit(), entry.getFinalValue(),
                        report.getSubmittedBy().getName(), report.getConfirmedAt()
                ))).toList();
        return new ExcelReportData(type, period, generatedAt, rows);
    }

    private String fileName(GeneratedReportType type, ReportPeriod period, int version) {
        String dates = period.start().equals(period.end())
                ? period.start().toString()
                : period.start() + "_to_" + period.end();
        return "FactoryFlow_" + type.name() + "_" + dates + "_v" + version + ".xlsx";
    }

    public record DownloadedGeneratedReport(String fileName, StoredReportFile file) { }
}
