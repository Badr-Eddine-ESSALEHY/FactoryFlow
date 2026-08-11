package com.factoryflow.shared.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.factoryflow.shared.api.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsMalformedJsonToTheCanonicalErrorEnvelope() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/reports/drafts");
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "Malformed JSON",
                new MockHttpInputMessage(new byte[0])
        );

        ResponseEntity<ApiErrorResponse> response = handler.handleMalformedRequest(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("MALFORMED_REQUEST");
        assertThat(response.getBody().path()).isEqualTo("/api/reports/drafts");
        assertThat(response.getBody().details()).isEmpty();
    }
}
