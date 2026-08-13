package com.factoryflow.app.feature.acquisition

import android.content.Intent
import android.net.Uri
import android.os.Build
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface SharedAcquisition {
    data class Text(val value: String) : SharedAcquisition
    data class Image(val uri: Uri) : SharedAcquisition
}

@Singleton
class SharedAcquisitionStore @Inject constructor() {
    private val _content = MutableStateFlow<SharedAcquisition?>(null)
    val content = _content.asStateFlow()

    fun accept(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        when {
            intent.type == "text/plain" -> intent.getStringExtra(Intent.EXTRA_TEXT)
                ?.takeIf { it.isNotBlank() }
                ?.let { _content.value = SharedAcquisition.Text(it) }
            intent.type?.startsWith("image/") == true -> sharedUri(intent)
                ?.let { _content.value = SharedAcquisition.Image(it) }
        }
    }

    fun consume(): SharedAcquisition? = _content.value.also { _content.value = null }

    private fun sharedUri(intent: Intent): Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
    } else {
        @Suppress("DEPRECATION") intent.getParcelableExtra(Intent.EXTRA_STREAM)
    }
}
