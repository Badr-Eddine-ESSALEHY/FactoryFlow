package com.factoryflow.intelligence;
import static org.assertj.core.api.Assertions.assertThat;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
class MaintenanceIntelligenceMigrationContractTest {
    @Test void migrationDefinesProfilesJsonbSnapshotsIdempotentAlertsAndNotificationLink() throws Exception {
        String sql = Files.readString(Path.of("src/main/resources/db/migration/V13__create_maintenance_intelligence.sql"));
        assertThat(sql).contains("kpi_intelligence_profiles", "maintenance_intelligence_analyses", "result_snapshot JSONB",
                "maintenance_intelligence_alerts", "UNIQUE (source_entry_id, alert_type)",
                "related_intelligence_alert_id", "MAINTENANCE_INTELLIGENCE_ATTENTION");
    }
}
