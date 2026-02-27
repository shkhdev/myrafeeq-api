package uz.myrafeeq.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;
import uz.myrafeeq.api.configuration.AdminProperties;
import uz.myrafeeq.api.dto.response.ErrorResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminApiKeyFilter extends OncePerRequestFilter {

  static final String ADMIN_PATH_PREFIX = "/api/v1/admin/";
  private static final String ADMIN_API_KEY_HEADER = "X-Admin-Api-Key";
  private final AdminProperties adminProperties;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String apiKey = request.getHeader(ADMIN_API_KEY_HEADER);

    if (apiKey != null
        && MessageDigest.isEqual(
            apiKey.getBytes(StandardCharsets.UTF_8),
            adminProperties.getApiKey().getBytes(StandardCharsets.UTF_8))) {

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              "admin", null, List.of(new SimpleGrantedAuthority("ADMIN")));
      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(request, response);
    } else {
      log.warn("Invalid admin API key for path: {}", request.getRequestURI());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      ErrorResponse errorResponse =
          ErrorResponse.of(
              "UNAUTHORIZED",
              "Invalid or missing admin API key",
              Instant.now(),
              request.getRequestURI());
      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getServletPath().startsWith(ADMIN_PATH_PREFIX);
  }
}
