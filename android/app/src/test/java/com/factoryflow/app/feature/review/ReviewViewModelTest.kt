package com.factoryflow.app.feature.review

import androidx.lifecycle.SavedStateHandle
import com.factoryflow.app.*
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
}
