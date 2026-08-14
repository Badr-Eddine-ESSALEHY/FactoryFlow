package com.factoryflow.app.feature.review

import androidx.lifecycle.SavedStateHandle
import com.factoryflow.app.*
import java.math.BigDecimal
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {
    @get:Rule val dispatcher = MainDispatcherRule()
    @Test fun `user correction remains authoritative through confirmation`() = runTest(dispatcher.dispatcher) {
        val repository = FakeReportsRepository().apply { draftValue = reportDto(); updated = reportDto(); confirmed = reportDto("CONFIRMED") }
        val viewModel = ReviewViewModel(SavedStateHandle(mapOf("reportId" to "12")), repository)
        advanceUntilIdle(); viewModel.edit(1, "43,5"); var confirmedId: Long? = null; viewModel.confirm { confirmedId = it }; advanceUntilIdle()
        assertEquals(12L, confirmedId); assertFalse(viewModel.state.value.confirming)
    }

    @Test fun `composite percentage remains linked and editable through confirmation`() = runTest(dispatcher.dispatcher) {
        val compositeDraft = reportDto().copy(entries = reportDto().entries.map {
            it.copy(secondaryExtractedValue = BigDecimal("77"), secondaryCurrentValue = BigDecimal("77"), secondaryUnit = "%")
        })
        val repository = FakeReportsRepository().apply {
            draftValue = compositeDraft
            updated = compositeDraft
            confirmed = compositeDraft.copy(status = "CONFIRMED")
        }
        val viewModel = ReviewViewModel(SavedStateHandle(mapOf("reportId" to "12")), repository)
        advanceUntilIdle()

        viewModel.editSecondary(1, "78")
        viewModel.confirm { }
        advanceUntilIdle()

        assertEquals(BigDecimal("78"), repository.lastConfirmRequest!!.entries.single().secondaryFinalValue)
        assertEquals("77", viewModel.state.value.entries.single().secondaryExtractedValue)
    }

    @Test fun `explicitly missing optional value does not block confirmation`() {
        val missing = ReviewEntry(
            id = 1,
            kpiDefinitionId = 10,
            displayName = "Vrac",
            value = "",
            extractedValue = null,
            unit = "t",
            confidenceScore = "100",
            warnings = setOf("MISSING_VALUE"),
            sourceLabel = "Vrac",
            sourceLine = "Vrac: ---",
            edited = false,
            suggestedKpiDefinitionId = null,
            suggestedKpiDisplayName = null,
            suggestedKpiUnit = null,
            suggestionScore = null,
        )
        assertTrue(ReviewUiState(loading = false, report = reportDto(), entries = listOf(missing)).canConfirm)
    }
}
