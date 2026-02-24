package uz.myrafeeq.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import uz.myrafeeq.api.configuration.JwtProperties;
import uz.myrafeeq.api.exception.InvalidAuthException;

@Component
public final class JwtTokenProvider {

  private final SecretKey secretKey;
  private final Duration tokenTtl;

  public JwtTokenProvider(JwtProperties jwtProperties) {
    this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtProperties.secret()));
    this.tokenTtl = jwtProperties.ttl();
  }

  public String generateToken(Long telegramId, String firstName) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(telegramId.toString())
        .claim("firstName", firstName)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(tokenTtl)))
        .signWith(secretKey)
        .compact();
  }

  public Long validateAndExtractTelegramId(String token) {
    try {
      Claims claims =
          Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

      return Long.parseLong(claims.getSubject());
    } catch (JwtException | IllegalArgumentException _) {
      throw new InvalidAuthException("Invalid or expired JWT token");
    }
  }
}
