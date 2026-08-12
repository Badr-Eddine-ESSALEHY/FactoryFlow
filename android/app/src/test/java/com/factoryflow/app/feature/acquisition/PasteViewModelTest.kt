package com.factoryflow.app.feature.acquisition

import com.factoryflow.app.*
import com.factoryflow.app.core.network.dto.*
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PasteViewModelTest {
    @get:Rule val dispatcher = MainDispatcherRule()
    @Test fun `analysis creates a resumable draft and preserves unknown lines`() = runTest(dispatcher.dispatcher) {
        val repository = FakeReportsRepository().apply {
            analyzed = AnalyzeReportResponse("PASTE", "Température: 42\nNote équipe", 1, 0, 1,
                listOf(ParsedEntryDto("1", 10, "TEMP", "Température", "Température", "Température: 42", 42.toBigDecimal(), "°C", "°C", 0.98.toBigDecimal(), "HIGH")),
                listOf(ParsedUnknownLineDto("2", "Note équipe", "NO_MATCH")))
            created = reportDto()
        }
        val viewModel = PasteViewModel(repository); var id: Long? = null
        viewModel.text("Température: 42\nNote équipe"); viewModel.analyze { id = it }; advanceUntilIdle()
        assertEquals(12L, id); assertFalse(viewModel.state.value.analyzing)
    }
}
