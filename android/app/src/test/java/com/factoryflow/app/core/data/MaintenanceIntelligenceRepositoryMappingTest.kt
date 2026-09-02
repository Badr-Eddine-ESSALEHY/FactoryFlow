package com.factoryflow.app.core.data

import com.factoryflow.app.core.network.dto.IntelligenceOverviewDto
import com.factoryflow.app.core.network.dto.IntelligenceOverviewItemDto
import com.factoryflow.app.core.network.dto.IntelligenceProfileDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MaintenanceIntelligenceRepositoryMappingTest {
    @Test
    fun `overview mapping preserves profile identity configuration and absent analysis`() {
        val dto = IntelligenceOverviewDto(
            kpis = listOf(
                IntelligenceOverviewItemDto(
                    profile = IntelligenceProfileDto(
                        id = 2,
                        kpiDefinitionId = 17,
                        kpiCode = "PRESSION",
                        kpiDisplayName = "Pression réseau",
                        enabled = true,
                        expectedCadenceDays = 1,
                        forecastHorizon = 7,
                        seasonalPeriod = 7,
                        historyWindowDays = 180,
                        createdAt = "2026-08-01T09:00:00Z",
                        updatedAt = "2026-09-01T09:00:00Z",
                        version = 3,
                    ),
                    latestSuccessfulAnalysis = null,
                    latestRefreshAttempt = null,
                    alertCount = 0,
                ),
            ),
        )

        val mapped = dto.toModel().kpis.single()

        assertEquals(17L, mapped.profile.kpiDefinitionId)
        assertEquals(1, mapped.profile.expectedCadenceDays)
        assertEquals(7, mapped.profile.forecastHorizon)
        assertEquals(180, mapped.profile.historyWindowDays)
        assertNull(mapped.latestSuccessfulAnalysis)
    }
}
