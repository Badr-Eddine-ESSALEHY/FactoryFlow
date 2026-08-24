package com.factoryflow.ocr.application;

import com.factoryflow.ocr.domain.OcrHealth;
import com.factoryflow.ocr.domain.OcrProvider;
import com.factoryflow.ocr.domain.OcrResult;
import com.factoryflow.shared.error.ApiErrorCode;
import com.factoryflow.shared.error.ApiException;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OcrService {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private final OcrProvider provider;
    private final long maxUploadBytes;

    public OcrService(OcrProvider provider, @Value("${factoryflow.ocr.max-upload-bytes:10485760}") long maxUploadBytes) {
        this.provider = provider;
        this.maxUploadBytes = maxUploadBytes;
    }

    public OcrResult recognize(MultipartFile file) {
        validate(file);
        try {
            OcrResult result = provider.recognize(file.getBytes(), safeName(file.getOriginalFilename()), normalizedType(file));
            if (result.fullText().isBlank()) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, ApiErrorCode.OCR_NO_TEXT_DETECTED,
                        "Aucun texte exploitable n’a été détecté dans l’image.");
            }
            return result;
        } catch (ApiException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.OCR_INVALID_IMAGE,
                    "L’image transmise n’a pas pu être lue.");
        }
    }

    public OcrHealth health() { return provider.health(); }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.OCR_INVALID_IMAGE,
                    "Une image non vide est requise.");
        }
        if (file.getSize() > maxUploadBytes) {
            throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, ApiErrorCode.OCR_INVALID_IMAGE,
                    "L’image dépasse la taille maximale autorisée.");
        }
        if (!ALLOWED_TYPES.contains(normalizedType(file))) {
            throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ApiErrorCode.OCR_INVALID_IMAGE,
                    "Le format d’image doit être JPEG, PNG ou WebP.");
        }
    }

    private String normalizedType(MultipartFile file) {
        return file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
    }

    private String safeName(String value) {
        if (value == null || value.isBlank()) return "factoryflow-image";
        return value.replaceAll("[^A-Za-z0-9._-]", "_").substring(0, Math.min(value.length(), 100));
    }
}
