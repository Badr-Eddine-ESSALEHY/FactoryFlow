package com.factoryflow.auth.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("factoryflow.security.jwt")
public record JwtProperties(
        @NotBlank String secret,
        @NotNull Duration accessTokenTtl
) {
    public JwtProperties {
        if (secret != null && secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 characters");
        }
    }
}
