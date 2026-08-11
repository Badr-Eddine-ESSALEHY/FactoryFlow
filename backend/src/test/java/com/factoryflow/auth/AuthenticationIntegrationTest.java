package com.factoryflow.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.auth.persistence.UserAccountRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserAccountRepository users;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void loginIssuesJwtThatAuthenticatesCurrentUserEndpoint() throws Exception {
        String email = "auth-" + UUID.randomUUID() + "@example.com";
        UserAccount user = users.saveAndFlush(UserAccount.create(
                "Maintenance Engineer", email, passwordEncoder.encode("correct-password")
        ));

        String loginBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"correct-password"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(900))
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andReturn().getResponse().getContentAsString();

        JsonNode login = objectMapper.readTree(loginBody);
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + login.get("accessToken").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void rejectsWrongPasswordAndProtectsBusinessEndpoints() throws Exception {
        String email = "wrong-" + UUID.randomUUID() + "@example.com";
        users.saveAndFlush(UserAccount.create("Engineer", email, passwordEncoder.encode("correct-password")));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"wrong-password"}
                                """.formatted(email)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_INVALID_CREDENTIALS"));

        mockMvc.perform(get("/api/kpi-definitions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_UNAUTHORIZED"));
    }
}
