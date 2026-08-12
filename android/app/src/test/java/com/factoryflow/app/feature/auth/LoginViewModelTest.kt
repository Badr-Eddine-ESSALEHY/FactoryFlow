package com.factoryflow.app.feature.auth

import com.factoryflow.app.FakeAuthRepository
import com.factoryflow.app.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    @get:Rule val dispatcher = MainDispatcherRule()

    @Test fun `valid credentials authenticate and clear password`() = runTest(dispatcher.dispatcher) {
        val repository = FakeAuthRepository(); val viewModel = LoginViewModel(repository); var success = false
        viewModel.email("nadia@factoryflow.local"); viewModel.password("secret-pass"); viewModel.login { success = true }
        advanceUntilIdle()
        assertTrue(success); assertEquals(1, repository.loginCalls); assertEquals("", viewModel.state.value.password)
    }

    @Test fun `invalid fields are rejected locally`() = runTest(dispatcher.dispatcher) {
        val repository = FakeAuthRepository(); val viewModel = LoginViewModel(repository)
        viewModel.email("not-an-email"); viewModel.login { fail("must not authenticate") }
        advanceUntilIdle()
        assertTrue(viewModel.state.value.emailError); assertTrue(viewModel.state.value.passwordError); assertEquals(0, repository.loginCalls)
    }
}
