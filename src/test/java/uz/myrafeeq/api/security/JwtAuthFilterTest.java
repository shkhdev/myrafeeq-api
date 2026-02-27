package uz.myrafeeq.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.myrafeeq.api.exception.InvalidAuthException;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private FilterChain filterChain;

  private JwtAuthFilter filter;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
    filter = new JwtAuthFilter(jwtTokenProvider);
  }

  @Test
  void should_setAuthentication_when_validBearerToken() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/dashboard");
    request.setServletPath("/api/v1/dashboard");
    request.addHeader("Authorization", "Bearer valid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    given(jwtTokenProvider.validateAndExtractTelegramId("valid-token")).willReturn(123456789L);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
        .isEqualTo(123456789L);
  }

  @Test
  void should_continueWithoutAuth_when_invalidToken() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/dashboard");
    request.setServletPath("/api/v1/dashboard");
    request.addHeader("Authorization", "Bearer bad-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    given(jwtTokenProvider.validateAndExtractTelegramId("bad-token"))
        .willThrow(new InvalidAuthException("Invalid token"));

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void should_continueWithoutAuth_when_noAuthorizationHeader() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/dashboard");
    request.setServletPath("/api/v1/dashboard");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void should_skipFilter_when_publicPath() {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/token");
    request.setServletPath("/api/v1/auth/token");

    assertThat(filter.shouldNotFilter(request)).isTrue();
  }

  @Test
  void should_skipFilter_when_adminPath() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/stats");
    request.setServletPath("/api/v1/admin/stats");

    assertThat(filter.shouldNotFilter(request)).isTrue();
  }

  @Test
  void should_notSkipFilter_when_authenticatedPath() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/dashboard");
    request.setServletPath("/api/v1/dashboard");

    assertThat(filter.shouldNotFilter(request)).isFalse();
  }
}
