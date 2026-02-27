package uz.myrafeeq.api.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;
import uz.myrafeeq.api.configuration.RateLimitProperties;
import uz.myrafeeq.api.dto.response.ErrorResponse;

@Slf4j
@Component
@Order(10)
public class RateLimitFilter extends OncePerRequestFilter {

  private final RateLimitProperties properties;
  private final ObjectMapper objectMapper;
  private final Cache<String, AtomicInteger> requestCounts;

  public RateLimitFilter(RateLimitProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.requestCounts =
        Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(1)).maximumSize(100_000).build();
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return request.getServletPath().startsWith(AdminApiKeyFilter.ADMIN_PATH_PREFIX);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (!properties.isEnabled()) {
      filterChain.doFilter(request, response);
      return;
    }

    String key = resolveKey(request);
    int limit = resolveLimit();

    AtomicInteger count = requestCounts.get(key, _ -> new AtomicInteger(0));
    if (count.incrementAndGet() > limit) {
      log.warn("Rate limit exceeded for key={}", key);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setHeader("Retry-After", "60");
      ErrorResponse errorResponse =
          ErrorResponse.of(
              "RATE_LIMIT_EXCEEDED",
              "Too many requests. Please try again later.",
              Instant.now(),
              request.getRequestURI());
      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
      return;
    }

    filterChain.doFilter(request, response);
  }

  private String resolveKey(HttpServletRequest request) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Long userId) {
      return "user:" + userId;
    }
    String forwarded = request.getHeader("X-Forwarded-For");
    String ip = forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    return "ip:" + ip;
  }

  private int resolveLimit() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Long) {
      return properties.getAuthRequestsPerMinute();
    }
    return properties.getRequestsPerMinute();
  }
}
