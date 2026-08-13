package com.factoryflow.report.application;

import com.factoryflow.auth.application.AuthenticationService;
import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.kpi.persistence.KpiDefinitionRepository;
import com.factoryflow.report.api.ConfirmReportRequest;
import com.factoryflow.report.api.ConfirmationEntryRequest;
import com.factoryflow.report.api.DraftEntryRequest;
import com.factoryflow.report.api.DraftReportRequest;
import com.factoryflow.report.api.DraftUnknownLineRequest;
import com.factoryflow.report.api.ReportResponse;
import com.factoryflow.report.api.UnknownLineResolutionRequest;
import com.factoryflow.report.domain.KpiEntry;
import com.factoryflow.report.domain.MaintenanceReport;
import com.factoryflow.report.domain.ReportStatus;
import com.factoryflow.report.domain.ReportUnrecognizedLine;
import com.factoryflow.report.domain.UnknownLineResolution;
import com.factoryflow.report.persistence.MaintenanceReportRepository;
import com.factoryflow.shared.error.ApiErrorCode;
import com.factoryflow.shared.error.ApiException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportDraftService {

    private final MaintenanceReportRepository reports;
    private final KpiDefinitionRepository definitions;
    private final AuthenticationService authentication;

    public ReportDraftService(MaintenanceReportRepository reports, KpiDefinitionRepository definitions,
                              AuthenticationService authentication) {
        this.reports = reports;
        this.definitions = definitions;
        this.authentication = authentication;
    }

    @Transactional
    public ReportResponse create(String email, DraftReportRequest request) {
        UserAccount user = authentication.requireUser(email);
        MaintenanceReport report = MaintenanceReport.draft(user, request.effectiveDate(), request.source(), request.rawText());
        populate(report, request);
        return ReportResponse.from(reports.saveAndFlush(report));
    }

    @Transactional
    public ReportResponse update(String email, Long reportId, DraftReportRequest request) {
        MaintenanceReport report = requireOwnedDraft(email, reportId);
        report.replaceDraft(request.effectiveDate(), request.source(), request.rawText());
        populate(report, request);
        return ReportResponse.from(reports.saveAndFlush(report));
    }

    @Transactional(readOnly = true)
    public ReportResponse get(String email, Long reportId) {
        return ReportResponse.from(requireOwnedDraft(email, reportId));
    }

    @Transactional
    public ReportResponse confirm(String email, Long reportId, ConfirmReportRequest request) {
        MaintenanceReport report = requireOwnedDraft(email, reportId);
        if (report.getStatus() == ReportStatus.CONFIRMED) {
            throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.REPORT_ALREADY_CONFIRMED, "The report is already confirmed.");
        }

        Map<Long, ConfirmationEntryRequest> submitted = new HashMap<>();
        for (ConfirmationEntryRequest entry : request.entries()) {
            if (submitted.put(entry.kpiDefinitionId(), entry) != null) {
                validationFailure("A KPI definition may appear only once in confirmation data.");
            }
        }
        Set<Long> draftDefinitionIds = new HashSet<>();
        for (KpiEntry entry : report.getEntries()) {
            if (entry.getDefinition() == null) {
                validationFailure("Every draft candidate must be assigned to a KPI or removed before confirmation.");
            }
            Long definitionId = entry.getDefinition().getId();
            if (!draftDefinitionIds.add(definitionId)) {
                validationFailure("Duplicate KPI candidates must be resolved before confirmation.");
            }
            ConfirmationEntryRequest finalEntry = submitted.get(definitionId);
            if (finalEntry == null) {
                validationFailure("Confirmation data must include every retained draft KPI entry.");
            }
            entry.confirm(finalEntry.finalValue());
        }
        if (!draftDefinitionIds.equals(submitted.keySet())) {
            validationFailure("Confirmation data contains a KPI that is not present in the draft.");
        }

        Map<Long, UnknownLineResolutionRequest> resolutions = new HashMap<>();
        request.unrecognizedLineResolutions().forEach(value -> resolutions.put(value.lineId(), value));
        for (ReportUnrecognizedLine line : report.getUnrecognizedLines()) {
            UnknownLineResolutionRequest resolution = resolutions.get(line.getId());
            if (resolution != null) {
                line.resolve(resolution.resolution(), optionalDefinition(resolution.resolvedKpiDefinitionId()));
            }
            if (line.getResolution() == UnknownLineResolution.UNRESOLVED) {
                validationFailure("Every unrecognized line must be assigned or ignored before confirmation.");
            }
        }
        report.confirm();
        return ReportResponse.from(reports.saveAndFlush(report));
    }

    private void populate(MaintenanceReport report, DraftReportRequest request) {
        for (DraftEntryRequest entry : request.entries()) {
            report.addEntry(optionalDefinition(entry.kpiDefinitionId()), entry.sourceLabel(), entry.sourceLine(),
                    entry.extractedValue(), entry.currentValue(), entry.confidenceScore(), entry.editedByUser(),
                    entry.capturedUnit(), entry.warnings(), optionalDefinition(entry.suggestedKpiDefinitionId()),
                    entry.suggestionScore());
        }
        for (DraftUnknownLineRequest line : request.unrecognizedLines()) {
            try {
                report.addUnrecognizedLine(line.sourceLine(), line.resolution(), optionalDefinition(line.resolvedKpiDefinitionId()));
            } catch (IllegalArgumentException exception) {
                validationFailure(exception.getMessage());
            }
        }
    }

    private MaintenanceReport requireOwnedDraft(String email, Long reportId) {
        UserAccount user = authentication.requireUser(email);
        MaintenanceReport report = reports.findByIdAndSubmittedById(reportId, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.REPORT_NOT_FOUND, "Report not found."));
        if (report.getStatus() == ReportStatus.CONFIRMED) {
            throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.REPORT_ALREADY_CONFIRMED, "The report is already confirmed.");
        }
        return report;
    }

    private KpiDefinition optionalDefinition(Long id) {
        if (id == null) return null;
        return definitions.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.KPI_DEFINITION_NOT_FOUND, "KPI definition not found."));
    }

    private void validationFailure(String message) {
        throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.REPORT_VALIDATION_FAILED, message);
    }
}
