package uz.myrafeeq.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;
import uz.myrafeeq.api.configuration.RateLimitProperties;
import uz.myrafeeq.api.dto.response.ErrorResponse;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitFilter extends OncePerRequestFilter {

  private final RateLimitProperties properties;
  private final ObjectMapper objectMapper;
  private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (!properties.enabled()) {
      filterChain.doFilter(request, response);
      return;
    }

    String clientKey = resolveClientKey(request);
    int limit =
        request.getServletPath().startsWith("/api/auth")
            ? properties.authRequestsPerMinute()
            : properties.requestsPerMinute();

    Bucket bucket = buckets.computeIfAbsent(clientKey, _ -> new Bucket());

    if (!bucket.tryConsume(limit)) {
      log.warn("Rate limit exceeded for client: {}", clientKey);
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      objectMapper.writeValue(
          response.getWriter(),
          ErrorResponse.of("RATE_LIMITED", "Too many requests, please slow down"));
      return;
    }

    filterChain.doFilter(request, response);
  }

  private String resolveClientKey(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      return xff.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  @Scheduled(fixedRate = 120_000)
  void cleanupStaleBuckets() {
    long cutoff = System.currentTimeMillis() - 120_000;
    buckets.entrySet().removeIf(entry -> entry.getValue().lastAccess < cutoff);
  }

  private static class Bucket {
    private final AtomicInteger count = new AtomicInteger(0);
    private volatile long windowStart = System.currentTimeMillis();
    private volatile long lastAccess = System.currentTimeMillis();

    boolean tryConsume(int limit) {
      long now = System.currentTimeMillis();
      lastAccess = now;
      if (now - windowStart > 60_000) {
        count.set(0);
        windowStart = now;
      }
      return count.incrementAndGet() <= limit;
    }
  }
}
