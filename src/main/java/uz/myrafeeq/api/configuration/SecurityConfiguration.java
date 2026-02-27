package uz.myrafeeq.api.configuration;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;
import uz.myrafeeq.api.dto.response.ErrorResponse;
import uz.myrafeeq.api.security.AdminApiKeyFilter;
import uz.myrafeeq.api.security.JwtAuthFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

  public static final String[] PUBLIC_PATHS = {
    "/api/v1/auth/**",
    "/api/v1/prayer-times/by-location",
    "/api/v1/cities/**",
    "/swagger-ui/**",
    "/v3/api-docs/**",
    "/actuator/health",
    "/actuator/health/**",
    "/actuator/info",
    "/actuator/prometheus"
  };

  private final JwtAuthFilter jwtAuthFilter;
  private final AdminApiKeyFilter adminApiKeyFilter;
  private final CorsProperties corsProperties;
  private final ObjectMapper objectMapper;

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) {
    return http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(PUBLIC_PATHS)
                    .permitAll()
                    .requestMatchers("/api/v1/admin/**")
                    .hasAuthority("ADMIN")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(adminApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                    (request, response, authException) -> {
                      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                      ErrorResponse errorResponse =
                          ErrorResponse.of(
                              "UNAUTHORIZED",
                              "Authentication required",
                              Instant.now(),
                              request.getRequestURI());
                      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                    }))
        .build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(corsProperties.getAllowedOrigins());
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(
        List.of(
            "Authorization",
            "Content-Type",
            "X-Request-Id",
            "X-Admin-Api-Key",
            "Ngrok-Skip-Browser-Warning"));
    config.setExposedHeaders(List.of("X-Request-Id"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
  }
}
