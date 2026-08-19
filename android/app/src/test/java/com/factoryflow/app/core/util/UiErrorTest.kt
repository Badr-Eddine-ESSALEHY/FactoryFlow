package com.factoryflow.app.core.util

import com.factoryflow.app.R
import com.factoryflow.app.core.network.AppError
import org.junit.Assert.assertEquals
import org.junit.Test

class UiErrorTest {

    @Test
    fun documentGenerationFailureHasAPreciseUserMessage() {
        val error = AppError.Server("REPORT_GENERATION_FAILED", "Internal generator detail")

        assertEquals(R.string.document_generation_failed, error.toDocumentGenerationUiError().detail)
    }

    @Test
    fun unreadableOcrImageHasARecoverableUserMessage() {
        val error = AppError.Validation("OCR_IMAGE_UNREADABLE", "Private provider detail")

        assertEquals(R.string.ocr_image_unreadable, error.toOcrUiError().detail)
    }

    @Test
    fun documentDownloadFailureDoesNotExposeBackendDetail() {
        val error = AppError.Server("REPORT_STORAGE_FAILURE", "Private storage path")

        assertEquals(R.string.download_failed, error.toDocumentDownloadUiError().detail)
    }
}
