package com.factoryflow.ocr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.factoryflow.ocr.application.OcrService;
import com.factoryflow.ocr.domain.OcrHealth;
import com.factoryflow.ocr.domain.OcrProvider;
import com.factoryflow.ocr.domain.OcrResult;
import com.factoryflow.shared.error.ApiException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class OcrServiceTest {
    @Test
    void preservesLineBreaksNumbersConfidenceAndWarningsFromProvider() {
        OcrResult expected = new OcrResult("Vrac : 42,75 t\nHumidité : ---", List.of(
                new OcrResult.Line("Vrac : 42,75 t", new BigDecimal("0.97"), new OcrResult.BoundingBox(1, 2, 30, 12)),
                new OcrResult.Line("Humidité : ---", new BigDecimal("0.62"), new OcrResult.BoundingBox(1, 14, 30, 25))
        ), new BigDecimal("0.795"), "PaddleOCR PP-OCRv5 (fr)", 123, List.of("LOW_CONFIDENCE"));
        OcrService service = new OcrService(provider(expected), 1024);
        OcrResult result = service.recognize(new MockMultipartFile("image", "report.png", "image/png", new byte[]{1, 2, 3}));
        assertThat(result).isEqualTo(expected);
        assertThat(result.fullText()).contains("42,75", "---", "\n");
    }

    @Test
    void rejectsUnsupportedEmptyAndOversizedUploadsBeforeProviderInvocation() {
        OcrService service = new OcrService(provider(null), 2);
        assertThatThrownBy(() -> service.recognize(new MockMultipartFile("image", "x.gif", "image/gif", new byte[]{1})))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> service.recognize(new MockMultipartFile("image", "x.png", "image/png", new byte[0])))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> service.recognize(new MockMultipartFile("image", "x.png", "image/png", new byte[]{1, 2, 3})))
                .isInstanceOf(ApiException.class);
    }

    private OcrProvider provider(OcrResult result) {
        return new OcrProvider() {
            @Override public OcrResult recognize(byte[] image, String fileName, String mimeType) { return result; }
            @Override public OcrHealth health() { return new OcrHealth(true, "PaddleOCR PP-OCRv5 (fr)", "ready"); }
        };
    }
}
