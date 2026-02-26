package uz.myrafeeq.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uz.myrafeeq.api.configuration.JwtProperties;
import uz.myrafeeq.api.exception.InvalidAuthException;

class JwtTokenProviderTest {

  private static final String SECRET =
      Base64.getEncoder()
          .encodeToString(
              "test-secret-key-for-myrafeeq-api-testing-only-must-be-at-least-256-bits".getBytes());

  private JwtTokenProvider jwtTokenProvider;

  @BeforeEach
  void setUp() {
    jwtTokenProvider = new JwtTokenProvider(new JwtProperties(SECRET, Duration.ofHours(1)));
  }

  @Test
  void should_generateValidToken_when_validInput() {
    String token = jwtTokenProvider.generateToken(123456789L, "Doston");

    assertThat(token).isNotBlank();
    assertThat(token.split("\\.")).hasSize(3);
  }

  @Test
  void should_extractTelegramId_when_validToken() {
    String token = jwtTokenProvider.generateToken(123456789L, "Doston");

    Long telegramId = jwtTokenProvider.validateAndExtractTelegramId(token);

    assertThat(telegramId).isEqualTo(123456789L);
  }

  @Test
  void should_throwInvalidAuth_when_tokenIsInvalid() {
    assertThatThrownBy(() -> jwtTokenProvider.validateAndExtractTelegramId("invalid.token.here"))
        .isInstanceOf(InvalidAuthException.class)
        .hasMessageContaining("Invalid or expired JWT token");
  }

  @Test
  void should_throwInvalidAuth_when_tokenIsExpired() {
    JwtTokenProvider expiredProvider =
        new JwtTokenProvider(new JwtProperties(SECRET, Duration.ZERO));

    String token = expiredProvider.generateToken(123456789L, "Doston");

    assertThatThrownBy(() -> expiredProvider.validateAndExtractTelegramId(token))
        .isInstanceOf(InvalidAuthException.class);
  }

  @Test
  void should_throwInvalidAuth_when_tokenSignedWithDifferentKey() {
    String otherSecret =
        Base64.getEncoder()
            .encodeToString(
                "another-secret-key-for-testing-that-is-also-at-least-256-bits-long!!".getBytes());
    JwtTokenProvider otherProvider =
        new JwtTokenProvider(new JwtProperties(otherSecret, Duration.ofHours(1)));

    String token = otherProvider.generateToken(123456789L, "Doston");

    assertThatThrownBy(() -> jwtTokenProvider.validateAndExtractTelegramId(token))
        .isInstanceOf(InvalidAuthException.class);
  }

  @Test
  void should_preserveDifferentTelegramIds_when_multipleTokens() {
    String token1 = jwtTokenProvider.generateToken(111L, "User1");
    String token2 = jwtTokenProvider.generateToken(222L, "User2");

    assertThat(jwtTokenProvider.validateAndExtractTelegramId(token1)).isEqualTo(111L);
    assertThat(jwtTokenProvider.validateAndExtractTelegramId(token2)).isEqualTo(222L);
  }

  @Test
  void should_throwInvalidAuth_when_tokenIsNull() {
    assertThatThrownBy(() -> jwtTokenProvider.validateAndExtractTelegramId(null))
        .isInstanceOf(InvalidAuthException.class);
  }

  @Test
  void should_throwInvalidAuth_when_tokenIsEmpty() {
    assertThatThrownBy(() -> jwtTokenProvider.validateAndExtractTelegramId(""))
        .isInstanceOf(InvalidAuthException.class);
  }
}
