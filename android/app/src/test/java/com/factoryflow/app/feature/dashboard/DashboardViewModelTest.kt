package com.factoryflow.app.feature.dashboard

import com.factoryflow.app.MainDispatcherRule
import com.factoryflow.app.core.data.DashboardRepository
import com.factoryflow.app.core.network.dto.DashboardActivityDto
import com.factoryflow.app.core.network.dto.DashboardDto
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    @get:Rule val dispatcher = MainDispatcherRule()
    @Test fun `dashboard exposes real counts without local fabrication`() = runTest(dispatcher.dispatcher) {
        val expected = DashboardDto(
            businessDate = "2026-08-12",
            todayConfirmedReportCount = 2,
            todayDraftOrPendingReportCount = 1,
            todayGeneratedDocumentCount = 0,
            todayConfirmedMissingValueCount = 3,
            todayHasConfirmedReport = true,
            activityTrend = listOf(
                DashboardActivityDto(date = "2026-08-12", confirmedReportCount = 2, missingValueCount = 3),
            ),
        )
        val viewModel = DashboardViewModel(object : DashboardRepository { override suspend fun dashboard() = expected })
        advanceUntilIdle()
        assertEquals(expected, viewModel.state.value.data); assertFalse(viewModel.state.value.loading)
    }
}
