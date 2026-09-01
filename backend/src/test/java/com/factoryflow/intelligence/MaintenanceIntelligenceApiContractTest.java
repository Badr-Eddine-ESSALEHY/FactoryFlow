package com.factoryflow.intelligence;
import static org.assertj.core.api.Assertions.assertThat;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
class MaintenanceIntelligenceApiContractTest {
    @Test void securedControllerSeparatesOverviewDetailRefreshProfilesAndAlerts() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/factoryflow/intelligence/api/MaintenanceIntelligenceController.java"));
        String security = Files.readString(Path.of("src/main/java/com/factoryflow/shared/config/SecurityConfiguration.java"));
        assertThat(source).contains("@RequestMapping(\"/api/maintenance-intelligence\")", "@SecurityRequirement(name = \"bearerAuth\")",
                "@GetMapping(\"/overview\")", "@GetMapping(\"/kpis/{kpiId}\")", "@PostMapping(\"/kpis/{kpiId}/refresh\")",
                "@PutMapping(\"/kpis/{kpiId}/profile\")", "@GetMapping(\"/alerts\")", "@GetMapping(\"/alerts/{id}\")");
        assertThat(security).contains(".anyRequest().authenticated()");
    }
}
