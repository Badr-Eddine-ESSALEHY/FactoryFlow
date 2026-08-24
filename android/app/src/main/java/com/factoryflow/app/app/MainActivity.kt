package com.factoryflow.app.app

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.factoryflow.app.core.design.FactoryFlowTheme
import com.factoryflow.app.core.design.ThemePreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.factoryflow.app.feature.acquisition.SharedAcquisitionStore

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var sharedAcquisitions: SharedAcquisitionStore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sharedAcquisitions.accept(intent)
        setContent {
            var themeMode by remember { mutableStateOf(ThemePreferences.read(this)) }
            FactoryFlowTheme(themeMode) {
                FactoryFlowApp(
                    sharedAcquisitions = sharedAcquisitions,
                    themeMode = themeMode,
                    onThemeMode = { selected ->
                        ThemePreferences.write(this, selected)
                        themeMode = selected
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedAcquisitions.accept(intent)
    }
}
