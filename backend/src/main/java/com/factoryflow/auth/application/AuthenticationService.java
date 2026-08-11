package com.factoryflow.auth.application;

import com.factoryflow.auth.api.AuthenticatedUserResponse;
import com.factoryflow.auth.api.LoginRequest;
import com.factoryflow.auth.api.LoginResponse;
import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.auth.persistence.UserAccountRepository;
import com.factoryflow.shared.error.ApiErrorCode;
import com.factoryflow.shared.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;

    public AuthenticationService(UserAccountRepository users, PasswordEncoder passwordEncoder, JwtTokenService tokenService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(request.email()).orElse(null);
        if (user == null || !user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCode.AUTH_INVALID_CREDENTIALS, "Invalid email or password.");
        }
        return new LoginResponse(
                tokenService.createAccessToken(user),
                "Bearer",
                tokenService.expiresInSeconds(),
                AuthenticatedUserResponse.from(user)
        );
    }

    public UserAccount requireUser(String email) {
        return users.findByEmailIgnoreCase(email)
                .filter(UserAccount::isActive)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCode.AUTH_UNAUTHORIZED, "Authentication is required."));
    }
}
