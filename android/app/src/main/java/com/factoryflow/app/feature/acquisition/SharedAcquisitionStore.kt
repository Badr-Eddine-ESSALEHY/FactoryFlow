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
    data class Invalid(val reason: SharedAcquisitionError) : SharedAcquisition
}

enum class SharedAcquisitionError { MISSING_CONTENT, UNSUPPORTED_TYPE }

@Singleton
class SharedAcquisitionStore @Inject constructor() {
    private val _content = MutableStateFlow<SharedAcquisition?>(null)
    val content = _content.asStateFlow()
    private var lastFingerprint: String? = null

    fun accept(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        val acquisition = when {
            intent.type == "text/plain" -> intent.getStringExtra(Intent.EXTRA_TEXT)
                ?.takeIf { it.isNotBlank() }?.let(SharedAcquisition::Text)
                ?: SharedAcquisition.Invalid(SharedAcquisitionError.MISSING_CONTENT)
            intent.type?.startsWith("image/") == true -> sharedUri(intent)?.let(SharedAcquisition::Image)
                ?: SharedAcquisition.Invalid(SharedAcquisitionError.MISSING_CONTENT)
            else -> SharedAcquisition.Invalid(SharedAcquisitionError.UNSUPPORTED_TYPE)
        }
        // Android may deliver the same Intent instance through both creation and a lifecycle callback.
        // Identity-based deduplication still allows the user to intentionally share identical content later.
        val fingerprint = "${intent.type}:${System.identityHashCode(intent)}"
        if (fingerprint == lastFingerprint) return
        lastFingerprint = fingerprint
        _content.value = acquisition
    }

    fun consume(): SharedAcquisition? = _content.value.also { _content.value = null }

    private fun sharedUri(intent: Intent): Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
    } else {
        @Suppress("DEPRECATION") intent.getParcelableExtra(Intent.EXTRA_STREAM)
    }
}
