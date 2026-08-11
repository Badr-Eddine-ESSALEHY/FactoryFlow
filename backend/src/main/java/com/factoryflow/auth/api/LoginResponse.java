package com.factoryflow.auth.api;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        AuthenticatedUserResponse user
) {
}
