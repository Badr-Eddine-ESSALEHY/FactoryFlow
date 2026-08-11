package com.factoryflow.auth.api;

import com.factoryflow.auth.domain.UserAccount;

public record AuthenticatedUserResponse(Long id, String name, String email, boolean active) {
    public static AuthenticatedUserResponse from(UserAccount user) {
        return new AuthenticatedUserResponse(user.getId(), user.getName(), user.getEmail(), user.isActive());
    }
}
