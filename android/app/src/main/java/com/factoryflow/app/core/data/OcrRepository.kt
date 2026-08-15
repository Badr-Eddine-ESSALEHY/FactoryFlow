package com.factoryflow.app.core.data

import android.content.Context
import android.net.Uri
import com.factoryflow.app.core.network.ApiExecutor
import com.factoryflow.app.core.network.AppError
import com.factoryflow.app.core.network.FactoryFlowApi
import com.factoryflow.app.core.network.dto.OcrResultDto
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.ByteArrayOutputStream
import java.io.InputStream
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

interface OcrRepository { suspend fun recognize(uri: Uri): OcrResultDto }

@Singleton
class DefaultOcrRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val api: FactoryFlowApi,
    private val executor: ApiExecutor,
) : OcrRepository {
    override suspend fun recognize(uri: Uri): OcrResultDto = executor.execute {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri)?.lowercase() ?: "application/octet-stream"
        if (mime !in ALLOWED_TYPES) throw AppError.Validation("OCR_INVALID_IMAGE", "Unsupported image format")
        val bytes = resolver.openInputStream(uri)?.use(::readBounded)
            ?: error("Unable to read the selected image")
        if (bytes.isEmpty()) throw AppError.Validation("OCR_INVALID_IMAGE", "The selected image is empty")
        val body = bytes.toRequestBody(mime.toMediaType())
        api.recognizeImage(MultipartBody.Part.createFormData("image", "factoryflow-image", body))
    }

    private companion object {
        const val MAX_UPLOAD_BYTES = 10 * 1024 * 1024
        val ALLOWED_TYPES = setOf("image/jpeg", "image/png", "image/webp")

        fun readBounded(input: InputStream): ByteArray {
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                total += read
                if (total > MAX_UPLOAD_BYTES) throw AppError.Validation("OCR_INVALID_IMAGE", "The selected image is too large")
                output.write(buffer, 0, read)
            }
            return output.toByteArray()
        }
    }
}
