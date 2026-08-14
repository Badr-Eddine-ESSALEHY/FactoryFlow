package com.factoryflow.app.feature.reports

import androidx.lifecycle.SavedStateHandle
import com.factoryflow.app.FakeGeneratedReportsRepository
import com.factoryflow.app.FakeReportsRepository
import com.factoryflow.app.MainDispatcherRule
import com.factoryflow.app.core.network.dto.GeneratedReportDto
import com.factoryflow.app.reportDto
import java.io.File
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ConfirmedReportViewModelTest {
    @get:Rule val dispatcher = MainDispatcherRule()

    @Test
    fun `export uses confirmed effective date then downloads generated document`() = runTest(dispatcher.dispatcher) {
        val reports = FakeReportsRepository().apply { reportValue = reportDto(status = "CONFIRMED", id = 23) }
        val document = GeneratedReportDto(
            id = 91,
            type = "DAILY",
            format = "PDF",
            periodStart = "2026-08-12",
            periodEnd = "2026-08-12",
            origin = "MANUAL",
            generationStatus = "READY",
            emailDeliveryStatus = "NOT_REQUESTED",
            version = 1,
            generatedAt = "2026-08-12T09:00:00Z",
            fileName = "factoryflow-2026-08-12.pdf",
            generatedBy = 1,
            regeneratedFromId = null,
            scheduleId = null,
        )
        val file = File("factoryflow-2026-08-12.pdf")
        val generated = FakeGeneratedReportsRepository().apply {
            generatedValue = document
            downloadedFile = file
        }
        val viewModel = ConfirmedReportViewModel(
            SavedStateHandle(mapOf("reportId" to "23")),
            reports,
            generated,
        )
        advanceUntilIdle()

        viewModel.export("PDF")
        advanceUntilIdle()

        assertEquals(1, generated.generationRequests.size)
        with(generated.generationRequests.single()) {
            assertEquals("DAILY", type)
            assertEquals("PDF", format)
            assertEquals("2026-08-12", periodStart)
            assertEquals("2026-08-12", periodEnd)
        }
        assertEquals(listOf(document), generated.downloadedReports)
        assertSame(file, viewModel.state.value.sharedFile)
        assertNull(viewModel.state.value.exportingFormat)
    }

    @Test
    fun `draft cannot enter confirmed export flow`() = runTest(dispatcher.dispatcher) {
        val reports = FakeReportsRepository().apply { reportValue = reportDto(status = "DRAFT", id = 24) }
        val generated = FakeGeneratedReportsRepository()
        val viewModel = ConfirmedReportViewModel(
            SavedStateHandle(mapOf("reportId" to "24")),
            reports,
            generated,
        )
        advanceUntilIdle()

        viewModel.export("EXCEL")
        advanceUntilIdle()

        assertFalse(generated.generationRequests.isNotEmpty())
        assertNull(viewModel.state.value.generatedDocument)
    }
}
