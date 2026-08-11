package com.factoryflow.report.api;

import com.factoryflow.report.application.ReportQueryService;
import com.factoryflow.report.domain.ReportStatus;
import com.factoryflow.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@SecurityRequirement(name = "bearerAuth")
public class ReportQueryController {

    private final ReportQueryService service;

    public ReportQueryController(ReportQueryService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List maintenance report history using effective-date filters")
    public PageResponse<ReportSummaryResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) Long submittedBy,
            @PageableDefault(size = 20, sort = "submittedAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable
    ) {
        return service.findReports(effectiveDate, dateFrom, dateTo, status, submittedBy, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Return full maintenance report detail")
    public ReportResponse get(@PathVariable Long id) {
        return service.findReport(id);
    }
}
