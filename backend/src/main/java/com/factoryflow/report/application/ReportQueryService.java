package com.factoryflow.report.application;

import com.factoryflow.report.api.ReportResponse;
import com.factoryflow.report.api.ReportSummaryResponse;
import com.factoryflow.report.domain.MaintenanceReport;
import com.factoryflow.report.domain.ReportStatus;
import com.factoryflow.report.persistence.MaintenanceReportRepository;
import com.factoryflow.shared.api.PageResponse;
import com.factoryflow.shared.error.ApiErrorCode;
import com.factoryflow.shared.error.ApiException;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportQueryService {

    private final MaintenanceReportRepository reports;

    public ReportQueryService(MaintenanceReportRepository reports) {
        this.reports = reports;
    }

    @Transactional(readOnly = true)
    public PageResponse<ReportSummaryResponse> findReports(
            LocalDate effectiveDate,
            LocalDate dateFrom,
            LocalDate dateTo,
            ReportStatus status,
            Long submittedBy,
            Pageable pageable
    ) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR,
                    "dateFrom must be on or before dateTo.");
        }
        Specification<MaintenanceReport> specification = (root, query, builder) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("submittedBy");
            }
            List<Predicate> predicates = new ArrayList<>();
            if (effectiveDate != null) predicates.add(builder.equal(root.get("effectiveDate"), effectiveDate));
            if (dateFrom != null) predicates.add(builder.greaterThanOrEqualTo(root.get("effectiveDate"), dateFrom));
            if (dateTo != null) predicates.add(builder.lessThanOrEqualTo(root.get("effectiveDate"), dateTo));
            if (status != null) predicates.add(builder.equal(root.get("status"), status));
            if (submittedBy != null) predicates.add(builder.equal(root.get("submittedBy").get("id"), submittedBy));
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        return PageResponse.from(reports.findAll(specification, pageable), ReportSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public ReportResponse findReport(Long id) {
        MaintenanceReport report = reports.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.REPORT_NOT_FOUND, "Report not found."));
        return ReportResponse.from(report);
    }
}
