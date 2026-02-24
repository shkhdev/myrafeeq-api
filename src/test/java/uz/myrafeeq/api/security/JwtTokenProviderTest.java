package uz.myrafeeq.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import uz.myrafeeq.api.configuration.JwtProperties;
import uz.myrafeeq.api.exception.InvalidAuthException;

class JwtTokenProviderTest {

  // 256-bit key, Base64-encoded (32 bytes -> 44 Base64 chars)
  private static final String TEST_SECRET =
      Base64.getEncoder().encodeToString("ThisIsATestSecretKeyWith32Bytes!".getBytes());

  private static final String DIFFERENT_SECRET =
      Base64.getEncoder().encodeToString("AnotherDifferentSecretKey32Byte!".getBytes());

  private static final Duration TEST_TTL = Duration.ofHours(1);

  private JwtTokenProvider createProvider(String secret, Duration ttl) {
    JwtProperties properties = new JwtProperties(secret, ttl);
    return new JwtTokenProvider(properties);
  }

  @Test
  void generateTokenReturnsNonNull() {
    JwtTokenProvider provider = createProvider(TEST_SECRET, TEST_TTL);
    String token = provider.generateToken(123456789L, "John");
    assertThat(token).isNotNull().isNotEmpty();
  }

  @Test
  void validateExtractsCorrectTelegramId() {
    JwtTokenProvider provider = createProvider(TEST_SECRET, TEST_TTL);
    Long telegramId = 987654321L;
    String token = provider.generateToken(telegramId, "Alice");
    Long extracted = provider.validateAndExtractTelegramId(token);
    assertThat(extracted).isEqualTo(telegramId);
  }

  @Test
  void validateWithDifferentSecretThrows() {
    JwtTokenProvider provider = createProvider(TEST_SECRET, TEST_TTL);
    JwtTokenProvider otherProvider = createProvider(DIFFERENT_SECRET, TEST_TTL);

    String token = provider.generateToken(123L, "Bob");

    assertThatThrownBy(() -> otherProvider.validateAndExtractTelegramId(token))
        .isInstanceOf(InvalidAuthException.class);
  }

  @Test
  void validateExpiredTokenThrows() throws InterruptedException {
    // Use zero-duration TTL so the token expires immediately
    JwtTokenProvider provider = createProvider(TEST_SECRET, Duration.ZERO);
    String token = provider.generateToken(123L, "Charlie");

    // Small delay to ensure the token is definitely expired
    Thread.sleep(10);

    assertThatThrownBy(() -> provider.validateAndExtractTelegramId(token))
        .isInstanceOf(InvalidAuthException.class);
  }

  @Test
  void validateGarbageTokenThrows() {
    JwtTokenProvider provider = createProvider(TEST_SECRET, TEST_TTL);

    assertThatThrownBy(() -> provider.validateAndExtractTelegramId("not.a.valid.jwt.token"))
        .isInstanceOf(InvalidAuthException.class);
  }

  @Test
  void generateTokenContainsFirstNameClaim() {
    JwtTokenProvider provider = createProvider(TEST_SECRET, TEST_TTL);
    String token = provider.generateToken(555L, "Diana");

    // Decode the JWT to verify the firstName claim is present
    SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(TEST_SECRET));
    String firstName =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("firstName", String.class);

    assertThat(firstName).isEqualTo("Diana");
  }
}
