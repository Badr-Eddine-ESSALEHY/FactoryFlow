package com.factoryflow.report.api;

import com.factoryflow.report.application.ReportDraftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@SecurityRequirement(name = "bearerAuth")
public class ReportDraftController {

    private final ReportDraftService service;

    public ReportDraftController(ReportDraftService service) {
        this.service = service;
    }

    @PostMapping("/drafts")
    @Operation(summary = "Create a non-authoritative server-side report draft")
    public ResponseEntity<ReportResponse> create(Principal principal, @Valid @RequestBody DraftReportRequest request) {
        ReportResponse created = service.create(principal.getName(), request);
        return ResponseEntity.created(URI.create("/api/reports/" + created.id() + "/draft")).body(created);
    }

    @PutMapping("/{id}/draft")
    @Operation(summary = "Replace the editable state of an existing report draft")
    public ReportResponse update(Principal principal, @PathVariable Long id,
                                 @Valid @RequestBody DraftReportRequest request) {
        return service.update(principal.getName(), id, request);
    }

    @GetMapping("/{id}/draft")
    @Operation(summary = "Load resumable report draft state")
    public ReportResponse get(Principal principal, @PathVariable Long id) {
        return service.get(principal.getName(), id);
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm reviewed KPI values transactionally")
    public ReportResponse confirm(Principal principal, @PathVariable Long id,
                                  @Valid @RequestBody ConfirmReportRequest request) {
        return service.confirm(principal.getName(), id, request);
    }

    @DeleteMapping("/{id}/draft")
    @Operation(summary = "Delete an owned, non-authoritative report draft")
    public ResponseEntity<Void> delete(Principal principal, @PathVariable Long id) {
        service.delete(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
