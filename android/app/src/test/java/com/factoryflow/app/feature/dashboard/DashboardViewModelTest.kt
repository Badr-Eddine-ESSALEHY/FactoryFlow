package com.factoryflow.app.feature.dashboard

import com.factoryflow.app.MainDispatcherRule
import com.factoryflow.app.core.data.DashboardRepository
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
        val expected = DashboardDto("2026-08-12", 2, 1, 3, true)
        val viewModel = DashboardViewModel(object : DashboardRepository { override suspend fun dashboard() = expected })
        advanceUntilIdle()
        assertEquals(expected, viewModel.state.value.data); assertFalse(viewModel.state.value.loading)
    }
}
