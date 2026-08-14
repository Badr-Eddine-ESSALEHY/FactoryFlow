package com.factoryflow.app.feature.reports

import androidx.lifecycle.SavedStateHandle
import com.factoryflow.app.*
import com.factoryflow.app.core.network.dto.*
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {
    @get:Rule val dispatcher = MainDispatcherRule()
    @Test fun `report list preserves draft and confirmed distinction`() = runTest(dispatcher.dispatcher) {
        val repository = FakeReportsRepository().apply { reportList = PageDto(content = listOf(
            ReportSummaryDto(1, "DRAFT", "PASTE", "2026-08-12", "2026-08-12T08:00:00Z", null, SubmittedByDto(1, "Nadia"), 2, 1),
            ReportSummaryDto(2, "CONFIRMED", "MANUAL", "2026-08-11", "2026-08-11T08:00:00Z", "2026-08-11T08:05:00Z", SubmittedByDto(1, "Nadia"), 3, 0),
        )) }
        val viewModel = ReportsViewModel(repository); advanceUntilIdle()
        assertEquals(listOf("DRAFT", "CONFIRMED"), viewModel.state.value.reports.map { it.status })
    }

    @Test fun `draft detail deletion removes only the loaded draft`() = runTest(dispatcher.dispatcher) {
        val repository = FakeReportsRepository().apply { reportValue = reportDto(status = "DRAFT", id = 12) }
        val viewModel = ReportDetailViewModel(SavedStateHandle(mapOf("reportId" to "12")), repository)
        advanceUntilIdle()

        viewModel.deleteDraft()
        advanceUntilIdle()

        assertEquals(listOf(12L), repository.deletedDraftIds)
        assertTrue(viewModel.state.value.deleted)
        assertFalse(viewModel.state.value.deleting)
    }

    @Test fun `confirmed report detail cannot be deleted`() = runTest(dispatcher.dispatcher) {
        val repository = FakeReportsRepository().apply { reportValue = reportDto(status = "CONFIRMED", id = 13) }
        val viewModel = ReportDetailViewModel(SavedStateHandle(mapOf("reportId" to "13")), repository)
        advanceUntilIdle()

        viewModel.deleteDraft()
        advanceUntilIdle()

        assertTrue(repository.deletedDraftIds.isEmpty())
        assertFalse(viewModel.state.value.deleted)
    }
}
