package com.factoryflow.ocr;

import static org.assertj.core.api.Assertions.assertThat;

import com.factoryflow.ocr.infrastructure.PaddleOcrProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.web.client.RestClient;

@EnabledIfEnvironmentVariable(named = "OCR_INTEGRATION_TEST", matches = "true")
class PaddleOcrProviderIntegrationTest {
    @Test
    void realRuntimeRecognizesFrenchDecimalCompositeAndMissingTokens() throws Exception {
        String runtime = System.getenv().getOrDefault("OCR_RUNTIME_URL", "http://127.0.0.1:8091");
        Path fixture = Path.of(System.getenv().getOrDefault("OCR_INTEGRATION_FIXTURE",
                "ocr-runtime/qa/generated/clean_french.jpg"));
        PaddleOcrProvider provider = new PaddleOcrProvider(RestClient.builder(), runtime,
                Duration.ofSeconds(3), Duration.ofSeconds(60));
        assertThat(provider.health().ready()).isTrue();
        var result = provider.recognize(Files.readAllBytes(fixture), fixture.getFileName().toString(), "image/jpeg");
        assertThat(result.engine()).contains("PP-OCRv5");
        assertThat(result.fullText()).contains("42,75", "77108", "77", "---");
        assertThat(result.lines()).isNotEmpty().allSatisfy(line -> assertThat(line.boundingBox()).isNotNull());
    }
}
