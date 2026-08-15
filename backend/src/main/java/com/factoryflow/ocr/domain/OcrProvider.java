package com.factoryflow.ocr.domain;

public interface OcrProvider {
    OcrResult recognize(byte[] image, String fileName, String mimeType);
    OcrHealth health();
}
