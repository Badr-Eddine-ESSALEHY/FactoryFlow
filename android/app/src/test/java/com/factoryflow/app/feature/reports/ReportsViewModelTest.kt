package com.factoryflow.app.feature.reports

import androidx.lifecycle.SavedStateHandle
import com.factoryflow.app.*
import com.factoryflow.app.core.network.dto.*
import java.time.LocalDate
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

    @Test fun `consolidated period resolver uses explicit calendar boundaries`() {
        assertEquals(
            ConsolidatedReportPeriod(LocalDate.of(2026, 8, 19), LocalDate.of(2026, 8, 19)),
            resolveConsolidatedReportPeriod("DAILY", LocalDate.of(2026, 8, 19), null, null),
        )
        assertEquals(
            ConsolidatedReportPeriod(LocalDate.of(2026, 8, 17), LocalDate.of(2026, 8, 23)),
            resolveConsolidatedReportPeriod("WEEKLY", LocalDate.of(2026, 8, 19), null, null),
        )
        assertEquals(
            ConsolidatedReportPeriod(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)),
            resolveConsolidatedReportPeriod("MONTHLY", LocalDate.of(2026, 2, 12), null, null),
        )
        assertEquals(
            ConsolidatedReportPeriod(LocalDate.of(2026, 7, 30), LocalDate.of(2026, 8, 4)),
            resolveConsolidatedReportPeriod(
                "CUSTOM",
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 7, 30),
                LocalDate.of(2026, 8, 4),
            ),
        )
        assertNull(
            resolveConsolidatedReportPeriod(
                "CUSTOM",
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 4),
                LocalDate.of(2026, 7, 30),
            ),
        )
    }

    @Test fun `consolidated generation requests both selected formats for the same period`() =
        runTest(dispatcher.dispatcher) {
            val generatedDocument = GeneratedReportDto(
                40,
                "WEEKLY",
                "PDF",
                "2026-08-17",
                "2026-08-23",
                "MANUAL",
                "READY",
                "NOT_REQUESTED",
                1,
                "2026-08-23T18:00:00Z",
                "FactoryFlow_WEEKLY.pdf",
                1,
                null,
                null,
            )
            val repository = FakeGeneratedReportsRepository().apply {
                generatedValue = generatedDocument
                listValue = PageDto(content = listOf(generatedDocument))
            }
            val viewModel = GeneratedListViewModel(repository)
            advanceUntilIdle()

            viewModel.generate(
                "WEEKLY",
                setOf("PDF", "EXCEL"),
                ConsolidatedReportPeriod(LocalDate.of(2026, 8, 17), LocalDate.of(2026, 8, 23)),
            )
            advanceUntilIdle()

            assertEquals(listOf("EXCEL", "PDF"), repository.generationRequests.map { it.format })
            assertTrue(repository.generationRequests.all {
                it.type == "WEEKLY" && it.periodStart == "2026-08-17" && it.periodEnd == "2026-08-23"
            })
            assertTrue(viewModel.state.value.generationCompleted)
            assertFalse(viewModel.state.value.generating)
        }
}
