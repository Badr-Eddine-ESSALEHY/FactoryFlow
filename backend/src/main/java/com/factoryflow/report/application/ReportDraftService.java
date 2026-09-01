package com.factoryflow.report.application;

import com.factoryflow.auth.application.AuthenticationService;
import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.kpi.application.KpiDefinitionService;
import com.factoryflow.kpi.persistence.KpiDefinitionRepository;
import com.factoryflow.notification.application.NotificationService;
import com.factoryflow.notification.domain.NotificationType;
import com.factoryflow.report.api.ConfirmReportRequest;
import com.factoryflow.report.api.ConfirmationEntryRequest;
import com.factoryflow.report.api.DraftEntryRequest;
import com.factoryflow.report.api.DraftReportRequest;
import com.factoryflow.report.api.DraftUnknownLineRequest;
import com.factoryflow.report.api.ReportResponse;
import com.factoryflow.report.api.UnknownLineResolutionRequest;
import com.factoryflow.report.domain.UnknownLineKind;
import com.factoryflow.report.domain.KpiEntry;
import com.factoryflow.report.domain.MaintenanceReport;
import com.factoryflow.report.domain.ReportStatus;
import com.factoryflow.report.domain.ReportUnrecognizedLine;
import com.factoryflow.report.domain.UnknownLineResolution;
import com.factoryflow.report.persistence.MaintenanceReportRepository;
import com.factoryflow.shared.error.ApiErrorCode;
import com.factoryflow.shared.error.ApiException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportDraftService {

    private final MaintenanceReportRepository reports;
    private final KpiDefinitionRepository definitions;
    private final AuthenticationService authentication;
    private final NotificationService notifications;
    private final KpiDefinitionService kpiDefinitionService;
    private final ApplicationEventPublisher events;

    public ReportDraftService(MaintenanceReportRepository reports, KpiDefinitionRepository definitions,
                              AuthenticationService authentication, NotificationService notifications,
                              KpiDefinitionService kpiDefinitionService, ApplicationEventPublisher events) {
        this.reports = reports;
        this.definitions = definitions;
        this.authentication = authentication;
        this.notifications = notifications;
        this.kpiDefinitionService = kpiDefinitionService;
        this.events = events;
    }

    @Transactional
    public ReportResponse create(String email, DraftReportRequest request) {
        UserAccount user = authentication.requireUser(email);
        MaintenanceReport report = MaintenanceReport.draft(user, request.effectiveDate(), request.source(), request.rawText());
        populate(report, request);
        MaintenanceReport saved = reports.saveAndFlush(report);
        boolean requiresAttention = saved.getUnrecognizedLines().stream()
                .anyMatch(line -> line.getResolution() == UnknownLineResolution.UNRESOLVED)
                || saved.getEntries().stream()
                .anyMatch(entry -> entry.getDefinition() == null || entry.getCurrentValue() == null || !entry.getWarningCodes().isEmpty());
        if (requiresAttention) {
            notifications.notify(user, NotificationType.REVIEW_REQUIRED, "Vérification requise",
                    "Le rapport du " + saved.getEffectiveDate() + " contient des éléments à vérifier.", saved.getId(), null);
        }
        return ReportResponse.from(saved);
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
    public void delete(String email, Long reportId) {
        MaintenanceReport report = requireOwnedDraft(email, reportId);
        reports.delete(report);
        reports.flush();
    }

    @Transactional
    public ReportResponse addDetectedKpi(String email, Long reportId, Long entryId) {
        MaintenanceReport report = requireOwnedDraft(email, reportId);
        KpiEntry entry = report.getEntries().stream()
                .filter(candidate -> candidate.getId().equals(entryId))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.REPORT_NOT_FOUND, "Draft entry not found."));
        if (entry.getDefinition() == null) {
            KpiDefinition definition = kpiDefinitionService.resolveOrCreate(entry.getSourceLabel(), entry.getCapturedUnit());
            entry.assignDefinition(definition);
        }
        return ReportResponse.from(reports.saveAndFlush(report));
    }

    @Transactional
    public ReportResponse ignoreSafeUnrecognizedLines(String email, Long reportId) {
        MaintenanceReport report = requireOwnedDraft(email, reportId);
        report.getUnrecognizedLines().stream()
                .filter(line -> line.getResolution() == UnknownLineResolution.UNRESOLVED)
                .filter(ReportUnrecognizedLine::isSafeToIgnore)
                .forEach(line -> line.resolve(UnknownLineResolution.IGNORED, null));
        return ReportResponse.from(reports.saveAndFlush(report));
    }

    @Transactional
    public ReportResponse resolveUnrecognizedLine(String email, Long reportId, Long lineId,
                                                   UnknownLineResolutionRequest request) {
        if (!lineId.equals(request.lineId())) {
            validationFailure("The unknown-line identifier does not match the request path.");
        }
        MaintenanceReport report = requireOwnedDraft(email, reportId);
        ReportUnrecognizedLine line = report.getUnrecognizedLines().stream()
                .filter(candidate -> candidate.getId().equals(lineId))
                .findFirst()
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, ApiErrorCode.REPORT_NOT_FOUND, "Unknown draft line not found."));
        line.resolve(request.resolution(), optionalDefinition(request.resolvedKpiDefinitionId()));
        return ReportResponse.from(reports.saveAndFlush(report));
    }

    @Transactional
    public ReportResponse removeEntry(String email, Long reportId, Long entryId) {
        MaintenanceReport report = requireOwnedDraft(email, reportId);
        KpiEntry removed;
        try {
            removed = report.removeEntry(entryId);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.REPORT_NOT_FOUND, exception.getMessage());
        }
        if (removed.getSourceLine() != null && !removed.getSourceLine().isBlank()) {
            report.addUnrecognizedLine(
                    removed.getSourceLine(), UnknownLineResolution.IGNORED, null,
                    UnknownLineKind.KPI_LIKE, "REMOVED_EXTRACTION", false);
        }
        return ReportResponse.from(reports.saveAndFlush(report));
    }

    @Transactional
    public ReportResponse confirm(String email, Long reportId, ConfirmReportRequest request) {
        MaintenanceReport report = requireOwnedDraft(email, reportId);
        if (report.getStatus() == ReportStatus.CONFIRMED) {
            throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.REPORT_ALREADY_CONFIRMED, "The report is already confirmed.");
        }

        Map<Long, ConfirmationEntryRequest> submitted = new HashMap<>();
        for (ConfirmationEntryRequest entry : request.entries()) {
            if (submitted.put(entry.entryId(), entry) != null) {
                validationFailure("A draft observation may appear only once in confirmation data.");
            }
        }
        Set<Long> draftEntryIds = new java.util.HashSet<>();
        for (KpiEntry entry : report.getEntries()) {
            if (entry.getDefinition() == null) {
                validationFailure("Every draft candidate must be assigned to a KPI or removed before confirmation.");
            }
            if (entry.getWarningCodes().stream().anyMatch(code -> !"MISSING_VALUE".equals(code))) {
                validationFailure("Every review warning must be explicitly validated before confirmation.");
            }
            Long entryId = entry.getId();
            draftEntryIds.add(entryId);
            ConfirmationEntryRequest finalEntry = submitted.get(entryId);
            if (finalEntry == null) {
                validationFailure("Confirmation data must include every retained draft KPI entry.");
            }
            if (!entry.getDefinition().getId().equals(finalEntry.kpiDefinitionId())) {
                validationFailure("Confirmation data changed the KPI assigned to a draft observation.");
            }
            entry.confirm(finalEntry.finalValue(), finalEntry.secondaryFinalValue());
        }
        if (!draftEntryIds.equals(submitted.keySet())) {
            validationFailure("Confirmation data contains an observation that is not present in the draft.");
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
        MaintenanceReport confirmed = reports.saveAndFlush(report);
        notifications.notify(confirmed.getSubmittedBy(), NotificationType.REPORT_CONFIRMED,
                "Rapport confirmé", "Le rapport du " + confirmed.getEffectiveDate() + " est maintenant officiel.",
                confirmed.getId(), null);
        events.publishEvent(new ReportConfirmedEvent(confirmed.getId(), confirmed.getEffectiveDate(),
                confirmed.getEntries().stream().map(entry -> entry.getDefinition().getId()).collect(java.util.stream.Collectors.toSet())));
        return ReportResponse.from(confirmed);
    }

    private void populate(MaintenanceReport report, DraftReportRequest request) {
        for (DraftEntryRequest entry : request.entries()) {
            report.addEntry(optionalDefinition(entry.kpiDefinitionId()), entry.sourceLabel(), entry.sourceLine(),
                    entry.extractedValue(), entry.currentValue(), entry.confidenceScore(), entry.editedByUser(),
                    entry.capturedUnit(), entry.warnings(), optionalDefinition(entry.suggestedKpiDefinitionId()),
                    entry.suggestionScore(), entry.suggestionStrength(), entry.suggestionMatchMethod(),
                    entry.secondaryExtractedValue(), entry.secondaryCurrentValue(),
                    entry.secondaryUnit());
        }
        for (DraftUnknownLineRequest line : request.unrecognizedLines()) {
            try {
                report.addUnrecognizedLine(
                        line.sourceLine(), line.resolution(), optionalDefinition(line.resolvedKpiDefinitionId()),
                        line.kind(), line.classificationReason(), line.safeToIgnore());
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
