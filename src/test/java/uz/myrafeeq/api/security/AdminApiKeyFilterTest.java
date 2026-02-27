package uz.myrafeeq.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.ObjectMapper;
import uz.myrafeeq.api.configuration.AdminProperties;

@ExtendWith(MockitoExtension.class)
class AdminApiKeyFilterTest {

  private static final String VALID_API_KEY = "test-admin-api-key";

  @Mock private FilterChain filterChain;

  private AdminApiKeyFilter filter;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
    AdminProperties adminProperties = new AdminProperties(VALID_API_KEY);
    filter = new AdminApiKeyFilter(adminProperties, new ObjectMapper());
  }

  @Test
  void should_authenticate_when_validApiKey() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/stats");
    request.setServletPath("/api/v1/admin/stats");
    request.addHeader("X-Admin-Api-Key", VALID_API_KEY);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
        .anyMatch(a -> a.getAuthority().equals("ADMIN"));
  }

  @Test
  void should_return401_when_invalidApiKey() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/stats");
    request.setServletPath("/api/v1/admin/stats");
    request.addHeader("X-Admin-Api-Key", "wrong-key");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(response.getContentAsString()).contains("UNAUTHORIZED");
  }

  @Test
  void should_return401_when_missingApiKey() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/stats");
    request.setServletPath("/api/v1/admin/stats");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  void should_skipFilter_when_nonAdminPath() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/cities");
    request.setServletPath("/api/v1/cities");

    assertThat(filter.shouldNotFilter(request)).isTrue();
  }

  @Test
  void should_notSkipFilter_when_adminPath() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/cities");
    request.setServletPath("/api/v1/admin/cities");

    assertThat(filter.shouldNotFilter(request)).isFalse();
  }
}
