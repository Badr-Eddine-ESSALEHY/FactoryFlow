package com.factoryflow.app.feature.auth

import com.factoryflow.app.FakeAuthRepository
import com.factoryflow.app.MainDispatcherRule
import com.factoryflow.app.core.network.AppError
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SessionViewModelTest {
    @get:Rule val dispatcher = MainDispatcherRule()

    @Test fun `connectivity failure keeps credentials and exposes retry state`() = runTest(dispatcher.dispatcher) {
        val repository = FakeAuthRepository().apply {
            hasStoredSession = true
            currentUserResult = Result.failure(AppError.NetworkUnavailable)
        }

        val viewModel = SessionViewModel(repository)
        advanceUntilIdle()

        assertTrue(viewModel.state.value is SessionUiState.RestoreFailed)
        assertEquals(0, repository.logoutCalls)
    }

    @Test fun `unauthorized restore is distinguished as an expired session`() = runTest(dispatcher.dispatcher) {
        val repository = FakeAuthRepository().apply {
            hasStoredSession = true
            currentUserResult = Result.failure(AppError.Unauthorized)
        }

        val viewModel = SessionViewModel(repository)
        advanceUntilIdle()

        assertEquals(SessionUiState.SignedOut(expired = true), viewModel.state.value)
    }
}
