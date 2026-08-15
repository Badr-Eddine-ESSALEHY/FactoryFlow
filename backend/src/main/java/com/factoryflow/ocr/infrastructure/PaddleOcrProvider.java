package com.factoryflow.ocr.infrastructure;

import com.factoryflow.ocr.domain.OcrHealth;
import com.factoryflow.ocr.domain.OcrProvider;
import com.factoryflow.ocr.domain.OcrResult;
import com.factoryflow.shared.error.ApiErrorCode;
import com.factoryflow.shared.error.ApiException;
import java.time.Duration;
import java.net.http.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class PaddleOcrProvider implements OcrProvider {
    private final RestClient client;
    private final String runtimeUrl;

    public PaddleOcrProvider(RestClient.Builder builder,
                             @Value("${factoryflow.ocr.runtime-url}") String runtimeUrl,
                             @Value("${factoryflow.ocr.connect-timeout:PT3S}") Duration connectTimeout,
                             @Value("${factoryflow.ocr.request-timeout:PT45S}") Duration requestTimeout) {
        this.runtimeUrl = runtimeUrl.replaceAll("/+$", "");
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(connectTimeout).build());
        requestFactory.setReadTimeout(requestTimeout);
        this.client = builder.requestFactory(requestFactory).build();
    }

    @Override
    public OcrResult recognize(byte[] image, String fileName, String mimeType) {
        ByteArrayResource resource = new ByteArrayResource(image) {
            @Override public String getFilename() { return fileName; }
        };
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        body.add("mimeType", mimeType);
        try {
            OcrResult result = client.post().uri(runtimeUrl + "/v1/ocr")
                    .contentType(MediaType.MULTIPART_FORM_DATA).body(body).retrieve().body(OcrResult.class);
            if (result == null) throw new RestClientException("Empty OCR runtime response");
            return result;
        } catch (RestClientException exception) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, ApiErrorCode.OCR_PROVIDER_UNAVAILABLE,
                    "Le moteur OCR local est temporairement indisponible.");
        }
    }

    @Override
    public OcrHealth health() {
        try {
            OcrHealth health = client.get().uri(runtimeUrl + "/health").retrieve().body(OcrHealth.class);
            return health == null ? new OcrHealth(false, "PaddleOCR PP-OCRv5", "Réponse vide") : health;
        } catch (RestClientException exception) {
            return new OcrHealth(false, "PaddleOCR PP-OCRv5", "Moteur local indisponible");
        }
    }
}
