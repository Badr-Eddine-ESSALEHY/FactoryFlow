package com.factoryflow.generatedreport.application;

import com.factoryflow.auth.application.AuthenticationService;
import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.generatedreport.api.GenerateReportRequest;
import com.factoryflow.generatedreport.api.GeneratedReportResponse;
import com.factoryflow.generatedreport.domain.GeneratedReport;
import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.generatedreport.domain.GeneratedReportType;
import com.factoryflow.generatedreport.domain.GenerationOrigin;
import com.factoryflow.generatedreport.domain.EmailDeliveryStatus;
import com.factoryflow.generatedreport.domain.GenerationStatus;
import com.factoryflow.generatedreport.domain.ReportPeriod;
import com.factoryflow.generatedreport.persistence.GeneratedReportRepository;
import com.factoryflow.generatedreport.storage.ReportStorageService;
import com.factoryflow.generatedreport.storage.StoredReportFile;
import com.factoryflow.report.domain.MaintenanceReport;
import com.factoryflow.report.domain.ReportStatus;
import com.factoryflow.report.persistence.MaintenanceReportRepository;
import com.factoryflow.schedule.domain.ReportSchedule;
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
    private final PdfReportGenerator pdfGenerator;
    private final ReportStorageService storage;
    private final Clock clock;

    public GeneratedReportService(GeneratedReportRepository generatedReports,
                                  MaintenanceReportRepository maintenanceReports,
                                  AuthenticationService authentication,
                                  ExcelReportGenerator excelGenerator,
                                  PdfReportGenerator pdfGenerator,
                                  ReportStorageService storage,
                                  Clock clock) {
        this.generatedReports = generatedReports;
        this.maintenanceReports = maintenanceReports;
        this.authentication = authentication;
        this.excelGenerator = excelGenerator;
        this.pdfGenerator = pdfGenerator;
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
        return generateDocument(request.type(), request.format(), period, user, null, GenerationOrigin.MANUAL);
    }

    @Transactional
    public GeneratedReportResponse generateScheduled(ReportSchedule schedule, GeneratedReportFormat format,
                                                      ReportPeriod period) {
        return generateDocument(GeneratedReportType.valueOf(schedule.getType().name()), format, period,
                null, schedule, GenerationOrigin.SCHEDULED);
    }

    private GeneratedReportResponse generateDocument(GeneratedReportType type, GeneratedReportFormat format,
                                                       ReportPeriod period, UserAccount user,
                                                       ReportSchedule schedule, GenerationOrigin origin) {
        List<MaintenanceReport> sourceReports = maintenanceReports
                .findAllByStatusAndEffectiveDateBetweenOrderByEffectiveDateAscIdAsc(
                        ReportStatus.CONFIRMED, period.start(), period.end());
        GeneratedReport previous = origin == GenerationOrigin.MANUAL
                ? generatedReports.findFirstByTypeAndFormatAndPeriodStartAndPeriodEndAndOriginOrderByVersionDesc(
                        type, format, period.start(), period.end(), GenerationOrigin.MANUAL).orElse(null)
                : null;
        int version = previous == null ? 1 : previous.getVersion() + 1;
        Instant generatedAt = clock.instant();
        String fileName = fileName(type, format, period, version, schedule);
        ReportGenerationData data = toGenerationData(type, period, generatedAt, sourceReports);
        byte[] document;
        try {
            document = switch (format) {
                case EXCEL -> excelGenerator.generate(data);
                case PDF -> pdfGenerator.generate(data);
            };
        } catch (RuntimeException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.REPORT_GENERATION_FAILED,
                    "The " + format + " report could not be generated.");
        }
        String reference = storage.store(fileName, document);
        try {
            GeneratedReport report = GeneratedReport.ready(
                    type, format, period, generatedAt, reference, fileName, user,
                    version, previous, new LinkedHashSet<>(sourceReports), origin, schedule,
                    schedule != null && schedule.isEmailEnabled()
                            ? EmailDeliveryStatus.PENDING : EmailDeliveryStatus.NOT_REQUESTED
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
        return new DownloadedGeneratedReport(report.getFileName(), report.getFormat(), file);
    }

    private GeneratedReport requireReport(Long id) {
        return generatedReports.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.GENERATED_REPORT_NOT_FOUND,
                        "Generated report not found."));
    }

    private ReportGenerationData toGenerationData(GeneratedReportType type, ReportPeriod period, Instant generatedAt,
                                                   List<MaintenanceReport> reports) {
        List<ReportGenerationData.Row> rows = reports.stream().flatMap(report -> report.getEntries().stream().map(entry ->
                new ReportGenerationData.Row(
                        report.getEffectiveDate(), report.getId(), report.getSource(),
                        entry.getDefinition().getDisplayName(), entry.getDefinition().getUnit(), entry.getFinalValue(),
                        report.getSubmittedBy().getName(), report.getConfirmedAt()
                ))).toList();
        return new ReportGenerationData(type, period, generatedAt, rows);
    }

    private String fileName(GeneratedReportType type, GeneratedReportFormat format, ReportPeriod period, int version,
                            ReportSchedule schedule) {
        String dates = period.start().equals(period.end())
                ? period.start().toString()
                : period.start() + "_to_" + period.end();
        String extension = format == GeneratedReportFormat.PDF ? ".pdf" : ".xlsx";
        String scheduleSuffix = schedule == null ? "" : "_schedule-" + schedule.getId();
        return "FactoryFlow_" + type.name() + "_" + dates + scheduleSuffix + "_v" + version + extension;
    }

    public record DownloadedGeneratedReport(String fileName, GeneratedReportFormat format, StoredReportFile file) { }
}
