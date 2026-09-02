package com.factoryflow.app.feature.intelligence

import org.junit.Assert.assertNotEquals
import org.junit.Test

class MaintenanceIntelligenceOverviewKeysTest {
    @Test
    fun `kpi and alert identifiers cannot collide in the overview lazy list`() {
        assertNotEquals(overviewKpiKey(2L), overviewAlertKey(2L))
    }
}
