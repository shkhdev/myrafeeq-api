package uz.myrafeeq.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.ObjectMapper;
import uz.myrafeeq.api.configuration.RateLimitProperties;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

  @Mock private FilterChain filterChain;

  private RateLimitFilter filter;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
    RateLimitProperties properties = new RateLimitProperties(true, 3, 5);
    filter = new RateLimitFilter(properties, new ObjectMapper());
  }

  @Test
  void should_allowRequest_when_underLimit() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/cities");
    request.setServletPath("/api/v1/cities");
    request.setRemoteAddr("192.168.1.1");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void should_return429_when_rateLimitExceeded() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/cities");
    request.setServletPath("/api/v1/cities");
    request.setRemoteAddr("10.0.0.1");
    MockHttpServletResponse response;

    // Exhaust the limit (3 requests per minute for public)
    for (int i = 0; i < 3; i++) {
      response = new MockHttpServletResponse();
      filter.doFilterInternal(request, response, filterChain);
    }

    // 4th request should be rate limited
    response = new MockHttpServletResponse();
    filter.doFilterInternal(request, response, filterChain);

    assertThat(response.getStatus()).isEqualTo(429);
    assertThat(response.getHeader("Retry-After")).isEqualTo("60");
    assertThat(response.getContentAsString()).contains("RATE_LIMIT_EXCEEDED");
  }

  @Test
  void should_passThrough_when_rateLimitDisabled() throws Exception {
    RateLimitProperties disabledProps = new RateLimitProperties(false, 1, 1);
    RateLimitFilter disabledFilter = new RateLimitFilter(disabledProps, new ObjectMapper());

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/cities");
    request.setServletPath("/api/v1/cities");
    MockHttpServletResponse response = new MockHttpServletResponse();

    disabledFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void should_useHigherLimit_when_authenticated() throws Exception {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(123456789L, null, List.of()));

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/dashboard");
    request.setServletPath("/api/v1/dashboard");
    MockHttpServletResponse response;

    // Authenticated limit is 5, so 5 requests should pass
    for (int i = 0; i < 5; i++) {
      response = new MockHttpServletResponse();
      filter.doFilterInternal(request, response, filterChain);
      assertThat(response.getStatus()).isEqualTo(200);
    }

    // 6th should fail
    response = new MockHttpServletResponse();
    filter.doFilterInternal(request, response, filterChain);
    assertThat(response.getStatus()).isEqualTo(429);
  }

  @Test
  void should_skipFilter_when_adminPath() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/stats");
    request.setServletPath("/api/v1/admin/stats");

    assertThat(filter.shouldNotFilter(request)).isTrue();
  }

  @Test
  void should_useXForwardedFor_when_headerPresent() throws Exception {
    MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/api/v1/cities");
    request1.setServletPath("/api/v1/cities");
    request1.addHeader("X-Forwarded-For", "203.0.113.1, 10.0.0.1");

    // Exhaust rate limit for this IP
    for (int i = 0; i < 3; i++) {
      filter.doFilterInternal(request1, new MockHttpServletResponse(), filterChain);
    }

    // Same X-Forwarded-For should be rate limited
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilterInternal(request1, response, filterChain);
    assertThat(response.getStatus()).isEqualTo(429);

    // Different X-Forwarded-For should pass
    MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/api/v1/cities");
    request2.setServletPath("/api/v1/cities");
    request2.addHeader("X-Forwarded-For", "198.51.100.1");
    MockHttpServletResponse response2 = new MockHttpServletResponse();
    filter.doFilterInternal(request2, response2, filterChain);
    assertThat(response2.getStatus()).isEqualTo(200);
  }
}
