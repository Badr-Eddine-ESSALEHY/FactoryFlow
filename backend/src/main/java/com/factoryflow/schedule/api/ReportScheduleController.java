package com.factoryflow.schedule.api;

import com.factoryflow.schedule.application.ReportScheduleService;
import com.factoryflow.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report-schedules")
@SecurityRequirement(name = "bearerAuth")
public class ReportScheduleController {
    private final ReportScheduleService service;
    public ReportScheduleController(ReportScheduleService service) { this.service = service; }
    @GetMapping @Operation(summary = "List report schedules")
    public List<ReportScheduleResponse> list() { return service.list(); }
    @GetMapping("/{id}") @Operation(summary = "Return a report schedule")
    public ReportScheduleResponse get(@PathVariable Long id) { return service.get(id); }
    @PostMapping @Operation(summary = "Create a daily, weekly, or monthly report schedule")
    public ResponseEntity<ReportScheduleResponse> create(Principal principal, @Valid @RequestBody ReportScheduleRequest request) {
        var created = service.create(principal.getName(), request);
        return ResponseEntity.created(URI.create("/api/report-schedules/" + created.id())).body(created);
    }
    @PutMapping("/{id}") @Operation(summary = "Replace report schedule configuration")
    public ReportScheduleResponse update(@PathVariable Long id, @Valid @RequestBody ReportScheduleRequest request) {
        return service.update(id, request);
    }
    @PatchMapping("/{id}/enabled") @Operation(summary = "Enable or pause a report schedule")
    public ReportScheduleResponse setEnabled(@PathVariable Long id, @RequestBody ScheduleEnabledRequest request) {
        return service.setEnabled(id, request.enabled());
    }
    @GetMapping("/{id}/runs") @Operation(summary = "List persisted schedule execution outcomes")
    public PageResponse<ScheduleRunResponse> runs(@PathVariable Long id,
            @PageableDefault(size = 20, sort = "startedAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) { return service.runs(id, pageable); }
}
