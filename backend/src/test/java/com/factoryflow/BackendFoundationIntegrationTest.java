package com.factoryflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.auth.persistence.UserAccountRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BackendFoundationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void flywayCreatedTheUsersTable() {
        Integer migrationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1' AND success",
                Integer.class
        );
        Integer usersTableCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = current_schema()
                  AND table_name = 'users'
                """,
                Integer.class
        );

        assertThat(migrationCount).isEqualTo(1);
        assertThat(usersTableCount).isEqualTo(1);
    }

    @Test
    void persistsAUserWithABcryptPasswordHash() {
        String rawPassword = "local-test-password";
        String passwordHash = passwordEncoder.encode(rawPassword);
        String email = "engineer-" + UUID.randomUUID() + "@example.com";

        UserAccount saved = userAccountRepository.saveAndFlush(
                UserAccount.create("Maintenance Engineer", email.toUpperCase(), passwordHash)
        );

        UserAccount reloaded = userAccountRepository.findByEmailIgnoreCase(email).orElseThrow();

        assertThat(reloaded.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getEmail()).isEqualTo(email);
        assertThat(reloaded.getPasswordHash()).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, reloaded.getPasswordHash())).isTrue();
        assertThat(reloaded.isActive()).isTrue();
    }

    @Test
    void exposesPublicHealthAndOpenApiEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("FactoryFlow API"))
                .andExpect(jsonPath("$.info.version").value("0.1.0"));
    }
}
