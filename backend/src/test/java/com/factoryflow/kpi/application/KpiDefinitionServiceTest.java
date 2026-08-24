package com.factoryflow.kpi.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.factoryflow.auth.application.AuthenticationService;
import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.kpi.persistence.KpiDefinitionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class KpiDefinitionServiceTest {

    private final KpiDefinitionRepository definitions = mock(KpiDefinitionRepository.class);
    private final KpiDefinitionService service = new KpiDefinitionService(
            definitions,
            mock(AuthenticationService.class)
    );

    @Test
    void reusesNormalizedEquivalentInsteadOfCreatingDuplicateKpi() {
        KpiDefinition existing = KpiDefinition.create(
                "ENZYME_3",
                "Enzyme 3",
                null,
                null,
                null,
                null,
                true,
                List.of()
        );
        when(definitions.findAllByOrderByDisplayNameAsc()).thenReturn(List.of(existing));

        KpiDefinition resolved = service.resolveOrCreate("  énzyme   3  ", null);

        assertThat(resolved).isSameAs(existing);
        verify(definitions, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
    }
}
