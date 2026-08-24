package com.factoryflow.generatedreport.api;

import com.factoryflow.generatedreport.application.GeneratedReportService;
import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.generatedreport.domain.GeneratedReportType;
import com.factoryflow.generatedreport.domain.GenerationStatus;
import com.factoryflow.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/generated-reports")
@SecurityRequirement(name = "bearerAuth")
public class GeneratedReportController {

    private static final MediaType EXCEL = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private final GeneratedReportService service;

    public GeneratedReportController(GeneratedReportService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Generate a consolidated Excel or PDF report from all confirmed data in a period")
    public ResponseEntity<GeneratedReportResponse> generate(
            Principal principal,
            @Valid @RequestBody GenerateReportRequest request
    ) {
        GeneratedReportResponse created = service.generate(principal.getName(), request);
        return ResponseEntity.created(URI.create("/api/generated-reports/" + created.id())).body(created);
    }

    @PostMapping("/individual")
    @Operation(summary = "Export exactly one confirmed maintenance report as Excel or PDF")
    public ResponseEntity<GeneratedReportResponse> generateIndividual(
            Principal principal,
            @Valid @RequestBody IndividualReportExportRequest request
    ) {
        GeneratedReportResponse created = service.generateIndividual(principal.getName(), request);
        return ResponseEntity.created(URI.create("/api/generated-reports/" + created.id())).body(created);
    }

    @GetMapping
    @Operation(summary = "List generated report history")
    public PageResponse<GeneratedReportResponse> list(
            @RequestParam(required = false) GeneratedReportType type,
            @RequestParam(required = false) GeneratedReportFormat format,
            @RequestParam(required = false) GenerationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @PageableDefault(size = 20, sort = "generatedAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable
    ) {
        return service.list(type, format, status, dateFrom, dateTo, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Return generated report metadata and source provenance")
    public GeneratedReportResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/{id}/file")
    @Operation(summary = "Download a ready generated Excel or PDF file")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        GeneratedReportService.DownloadedGeneratedReport download = service.download(id);
        return ResponseEntity.ok()
                .contentType(download.format() == GeneratedReportFormat.PDF ? MediaType.APPLICATION_PDF : EXCEL)
                .contentLength(download.file().contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        (download.format() == GeneratedReportFormat.PDF ? "inline" : "attachment")
                                + "; filename=\"" + download.fileName().replace("\"", "") + "\"")
                .body(download.file().resource());
    }
}
