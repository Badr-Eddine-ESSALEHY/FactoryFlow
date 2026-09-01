package com.factoryflow.intelligence.infrastructure.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.factoryflow.intelligence.application.AnalyticalRuntimeRequest;
import com.factoryflow.intelligence.application.AnalyticalRuntimeResponse;
import com.factoryflow.intelligence.application.MaintenanceIntelligenceProperties;
import com.factoryflow.intelligence.application.MaintenanceIntelligenceProvider;
import com.factoryflow.intelligence.application.MaintenanceIntelligenceProviderException;
import java.net.http.HttpClient;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class PythonMaintenanceIntelligenceProvider implements MaintenanceIntelligenceProvider {
    private final RestClient client;
    private final String runtimeUrl;

    public PythonMaintenanceIntelligenceProvider(
            RestClient.Builder builder,
            MaintenanceIntelligenceProperties properties,
            ObjectMapper objectMapper
    ) {
        runtimeUrl = properties.runtimeUrl().replaceAll("/+$", "");
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .connectTimeout(properties.connectTimeout())
                        .version(HttpClient.Version.HTTP_1_1)
                        .build());
        requestFactory.setReadTimeout(properties.requestTimeout());
        client = builder.requestFactory(requestFactory)
                .messageConverters(converters -> {
                    converters.removeIf(MappingJackson2HttpMessageConverter.class::isInstance);
                    converters.add(new MappingJackson2HttpMessageConverter(
                            objectMapper.copy().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)));
                })
                .build();
    }

    @Override
    public AnalyticalRuntimeResponse analyze(AnalyticalRuntimeRequest request) {
        try {
            AnalyticalRuntimeResponse response = client.post()
                    .uri(runtimeUrl + "/v1/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AnalyticalRuntimeResponse.class);
            if (response == null) throw new MaintenanceIntelligenceProviderException("Empty analytical runtime response");
            return response;
        } catch (RestClientException exception) {
            throw new MaintenanceIntelligenceProviderException("Private analytical runtime is unavailable", exception);
        }
    }
}
