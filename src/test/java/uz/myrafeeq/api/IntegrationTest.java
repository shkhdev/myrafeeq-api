package uz.myrafeeq.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uz.myrafeeq.api.security.JwtTokenProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class IntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:17-alpine")
          .withDatabaseName("myrafeeq_test")
          .withUsername("test")
          .withPassword("test")
          .withInitScript("init-schema.sql");

  @Autowired MockMvc mockMvc;
  @Autowired JwtTokenProvider jwtTokenProvider;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", () -> postgres.getJdbcUrl() + "&currentSchema=myrafeeq");
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.liquibase.default-schema", () -> "myrafeeq");
  }

  private String bearerToken(Long telegramId) {
    return "Bearer " + jwtTokenProvider.generateToken(telegramId, "TestUser");
  }

  @Test
  void contextLoads() {
    // Verifies that the full application context starts successfully
    // with Testcontainers PostgreSQL and Liquibase migrations
  }

  @Test
  void publicEndpointsAccessible() throws Exception {
    mockMvc.perform(get("/api/cities/search").param("q", "Tash")).andExpect(status().isOk());
  }

  @Test
  void publicPrayerTimesByLocation() throws Exception {
    mockMvc
        .perform(
            get("/api/prayer-times/by-location").param("lat", "41.2995").param("lon", "69.2401"))
        .andExpect(status().isOk());
  }

  @Test
  void authenticatedEndpointRequiresAuth() throws Exception {
    mockMvc.perform(get("/api/prayer-tracking")).andExpect(status().isForbidden());
  }

  @Test
  void authenticatedEndpointWithValidToken() throws Exception {
    mockMvc
        .perform(get("/api/user/preferences").header("Authorization", bearerToken(123456789L)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("PREFERENCES_NOT_FOUND"));
  }

  @Test
  void actuatorHealthEndpoint() throws Exception {
    mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
  }

  @Test
  void citySearchReturnsResults() throws Exception {
    mockMvc
        .perform(get("/api/cities/search").param("q", "Tashkent"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cities").isArray())
        .andExpect(jsonPath("$.cities").isNotEmpty());
  }

  @Test
  void citySearchLimitValidation() throws Exception {
    mockMvc
        .perform(get("/api/cities/search").param("q", "test").param("limit", "0"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"));
  }

  @Test
  void prayerTimesByLocationValidation() throws Exception {
    mockMvc
        .perform(get("/api/prayer-times/by-location").param("lat", "100").param("lon", "69"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"));
  }

  @TestConfiguration
  static class JacksonTestConfig {

    @Bean
    ObjectMapper objectMapper() {
      return new ObjectMapper();
    }
  }
}
