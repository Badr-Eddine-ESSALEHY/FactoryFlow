package com.factoryflow.auth.api;

import com.factoryflow.auth.application.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Authenticate a maintenance engineer")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }

    @GetMapping("/users/me")
    @Operation(summary = "Return the authenticated maintenance engineer")
    @SecurityRequirement(name = "bearerAuth")
    public AuthenticatedUserResponse currentUser(Principal principal) {
        return AuthenticatedUserResponse.from(authenticationService.requireUser(principal.getName()));
    }
}
