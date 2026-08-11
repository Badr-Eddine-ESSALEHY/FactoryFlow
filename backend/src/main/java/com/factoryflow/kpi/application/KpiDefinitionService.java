package com.factoryflow.kpi.application;

import com.factoryflow.kpi.api.KpiDefinitionRequest;
import com.factoryflow.kpi.api.KpiDefinitionResponse;
import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.kpi.persistence.KpiDefinitionRepository;
import com.factoryflow.shared.error.ApiErrorCode;
import com.factoryflow.shared.error.ApiException;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KpiDefinitionService {

    private final KpiDefinitionRepository definitions;

    public KpiDefinitionService(KpiDefinitionRepository definitions) {
        this.definitions = definitions;
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

    @Transactional(readOnly = true)
    public List<KpiDefinition> activeDefinitions() {
        return definitions.findAllByActiveOrderByDisplayNameAsc(true);
    }
}
