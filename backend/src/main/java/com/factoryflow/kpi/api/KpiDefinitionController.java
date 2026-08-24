package com.factoryflow.kpi.api;

import com.factoryflow.kpi.application.KpiDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kpi-definitions")
@SecurityRequirement(name = "bearerAuth")
public class KpiDefinitionController {

    private final KpiDefinitionService service;

    public KpiDefinitionController(KpiDefinitionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List KPI definitions used by parsing and review")
    public List<KpiDefinitionResponse> list(@RequestParam(defaultValue = "true") Boolean active) {
        return service.list(active);
    }

    @PostMapping
    @Operation(summary = "Create a configurable KPI definition")
    public ResponseEntity<KpiDefinitionResponse> create(@Valid @RequestBody KpiDefinitionRequest request) {
        KpiDefinitionResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/kpi-definitions/" + created.id())).body(created);
    }

    @PostMapping("/{id}/aliases")
    @Operation(summary = "Persist an alias explicitly approved by the authenticated maintenance engineer")
    public KpiDefinitionResponse approveAlias(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody ApproveAliasRequest request
    ) {
        return service.approveAlias(principal.getName(), id, request.alias());
    }
}
