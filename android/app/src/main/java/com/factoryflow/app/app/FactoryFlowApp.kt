package com.factoryflow.app.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.LoadingPane
import com.factoryflow.app.core.navigation.AuthenticatedApp
import com.factoryflow.app.feature.auth.*

@Composable
fun FactoryFlowApp(viewModel: SessionViewModel = hiltViewModel()) {
    val session by viewModel.state.collectAsStateWithLifecycle()
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (val state = session) {
            SessionUiState.Loading -> LoadingPane(stringResource(R.string.app_name))
            SessionUiState.SignedOut -> LoginScreen(onAuthenticated = viewModel::restore)
            is SessionUiState.SignedIn -> AuthenticatedApp(state.user, viewModel::logout)
        }
    }
}
