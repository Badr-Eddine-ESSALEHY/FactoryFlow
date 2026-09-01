package com.factoryflow.intelligence;

import static org.assertj.core.api.Assertions.assertThat;

import com.factoryflow.intelligence.infrastructure.persistence.MaintenanceIntelligenceHistoryRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

class MaintenanceIntelligenceHistoryRepositoryContractTest {
    @Test
    void nativeHistoryQueryEnforcesConfirmedFinalValueTrustBoundary() throws Exception {
        Query query = MaintenanceIntelligenceHistoryRepository.class
                .getMethod("findConfirmedHistory", Long.class, LocalDate.class, LocalDate.class)
                .getAnnotation(Query.class);
        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value()).contains("r.status = 'CONFIRMED'", "e.final_value AS finalValue");
        assertThat(query.value()).doesNotContain("extracted_value", "current_value", "raw_text");
    }
}
