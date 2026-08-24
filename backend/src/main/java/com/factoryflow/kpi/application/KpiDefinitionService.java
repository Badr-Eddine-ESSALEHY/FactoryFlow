package com.factoryflow.kpi.application;

import com.factoryflow.auth.application.AuthenticationService;
import com.factoryflow.kpi.api.KpiDefinitionRequest;
import com.factoryflow.kpi.api.KpiDefinitionResponse;
import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.kpi.persistence.KpiDefinitionRepository;
import com.factoryflow.shared.text.TextNormalizer;
import com.factoryflow.shared.error.ApiErrorCode;
import com.factoryflow.shared.error.ApiException;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KpiDefinitionService {

    private final KpiDefinitionRepository definitions;
    private final AuthenticationService authentication;

    public KpiDefinitionService(KpiDefinitionRepository definitions, AuthenticationService authentication) {
        this.definitions = definitions;
        this.authentication = authentication;
    }

    @Transactional(readOnly = true)
    public List<KpiDefinitionResponse> list(Boolean active) {
        List<KpiDefinition> values = active == null
                ? definitions.findAllByOrderByDisplayNameAsc()
                : definitions.findAllByActiveOrderByDisplayNameAsc(active);
        return values.stream().map(KpiDefinitionResponse::from).toList();
    }

    @Transactional
    public KpiDefinitionResponse create(KpiDefinitionRequest request) {
        if (definitions.existsByCodeIgnoreCase(request.code())) {
            throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.KPI_CODE_ALREADY_EXISTS, "A KPI definition with this code already exists.");
        }
        try {
            KpiDefinition definition = KpiDefinition.create(
                    request.code(), request.displayName(), request.category(), request.unit(),
                    request.plausibleMin(), request.plausibleMax(), request.active(), request.aliases()
            );
            return KpiDefinitionResponse.from(definitions.saveAndFlush(definition));
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, "A KPI code or normalized alias already exists.");
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, exception.getMessage());
        }
    }

    @Transactional
    public KpiDefinition resolveOrCreate(String detectedLabel, String detectedUnit) {
        String label = requiredDetectedLabel(detectedLabel);
        String normalizedLabel = TextNormalizer.normalizeLabel(label);

        KpiDefinition equivalent = definitions.findAllByOrderByDisplayNameAsc().stream()
                .filter(KpiDefinition::isActive)
                .filter(definition -> equivalentLabel(definition, normalizedLabel))
                .findFirst()
                .orElse(null);
        if (equivalent != null) {
            return equivalent;
        }

        String baseCode = normalizedLabel.replace(' ', '_').toUpperCase(Locale.ROOT);
        if (baseCode.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, "The detected KPI label is invalid.");
        }
        baseCode = baseCode.substring(0, Math.min(baseCode.length(), 90));
        String code = availableCode(baseCode);

        try {
            return definitions.saveAndFlush(KpiDefinition.create(
                    code, label, null, detectedUnit, null, null, true, List.of()
            ));
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, "A KPI with an equivalent label already exists.");
        }
    }

    @Transactional
    public KpiDefinitionResponse approveAlias(String email, Long definitionId, String alias) {
        KpiDefinition definition = definitions.findById(definitionId)
                .filter(KpiDefinition::isActive)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.KPI_DEFINITION_NOT_FOUND, "KPI definition not found."));
        try {
            definition.addApprovedAlias(alias, authentication.requireUser(email));
            return KpiDefinitionResponse.from(definitions.saveAndFlush(definition));
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, "This normalized alias is already assigned to another KPI.");
        }
    }

    @Transactional(readOnly = true)
    public List<KpiDefinition> activeDefinitions() {
        return definitions.findAllByActiveOrderByDisplayNameAsc(true);
    }

    private boolean equivalentLabel(KpiDefinition definition, String normalizedLabel) {
        return normalizedLabel.equals(TextNormalizer.normalizeLabel(definition.getDisplayName()))
                || normalizedLabel.equals(TextNormalizer.normalizeLabel(definition.getCode()))
                || definition.getAliases().stream().anyMatch(alias -> normalizedLabel.equals(alias.getNormalizedAlias()));
    }

    private String availableCode(String baseCode) {
        String code = baseCode;
        int suffix = 2;
        while (definitions.existsByCodeIgnoreCase(code)) {
            code = baseCode + "_" + suffix++;
        }
        return code;
    }

    private String requiredDetectedLabel(String value) {
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, "A detected KPI label is required.");
        }
        return value.strip();
    }
}
