package com.factoryflow.app.feature.statistics

import com.factoryflow.app.MainDispatcherRule
import com.factoryflow.app.core.data.StatisticsRepository
import com.factoryflow.app.core.network.dto.*
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {
    @get:Rule val dispatcher = MainDispatcherRule()
    @Test fun `statistics keeps missing count separate from numeric samples`() = runTest(dispatcher.dispatcher) {
        val kpi = KpiStatisticsDto(1, "TEMP", "Température", "°C", 42.toBigDecimal(), 40.toBigDecimal(), 44.toBigDecimal(), 42.toBigDecimal(), 3, 4, 1, listOf(StatisticsPointDto("2026-08-12", 1, 42.toBigDecimal())))
        val repository = object : StatisticsRepository { override suspend fun statistics(kpiId: Long?, dateFrom: String, dateTo: String) = StatisticsDto(dateFrom, dateTo, listOf(kpi)) }
        val viewModel = StatisticsViewModel(repository); advanceUntilIdle()
        assertEquals(3L, viewModel.state.value.selected?.sampleCount); assertEquals(1L, viewModel.state.value.selected?.missingValueCount)
    }
}
