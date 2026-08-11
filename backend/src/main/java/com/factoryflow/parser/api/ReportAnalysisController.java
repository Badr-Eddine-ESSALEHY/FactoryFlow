package com.factoryflow.parser.api;

import com.factoryflow.parser.application.ReportAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@SecurityRequirement(name = "bearerAuth")
public class ReportAnalysisController {

    private final ReportAnalysisService service;

    public ReportAnalysisController(ReportAnalysisService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    @Operation(summary = "Analyze raw KPI text without persisting it")
    public AnalyzeReportResponse analyze(@Valid @RequestBody AnalyzeReportRequest request) {
        return service.analyze(request);
    }
}
