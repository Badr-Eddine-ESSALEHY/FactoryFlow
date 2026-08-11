package com.factoryflow.parser.application;

import com.factoryflow.kpi.application.KpiDefinitionService;
import com.factoryflow.parser.api.AnalyzeReportRequest;
import com.factoryflow.parser.api.AnalyzeReportResponse;
import com.factoryflow.report.domain.AcquisitionSource;
import com.factoryflow.shared.error.ApiErrorCode;
import com.factoryflow.shared.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportAnalysisService {

    private final KpiDefinitionService definitions;
    private final DeterministicKpiParser parser;

    public ReportAnalysisService(KpiDefinitionService definitions, DeterministicKpiParser parser) {
        this.definitions = definitions;
        this.parser = parser;
    }

    @Transactional(readOnly = true)
    public AnalyzeReportResponse analyze(AnalyzeReportRequest request) {
        if (request.source() == AcquisitionSource.MANUAL) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, "MANUAL input does not use text analysis.");
        }
        return parser.parse(request.rawText(), request.source(), definitions.activeDefinitions());
    }
}
