package uz.myrafeeq.api.service.auth;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uz.myrafeeq.api.configuration.TelegramProperties;
import uz.myrafeeq.api.dto.request.TelegramAuthRequest;
import uz.myrafeeq.api.dto.response.AuthResponse;
import uz.myrafeeq.api.dto.response.UserResponse;
import uz.myrafeeq.api.entity.UserEntity;
import uz.myrafeeq.api.exception.InvalidAuthException;
import uz.myrafeeq.api.mapper.UserMapper;
import uz.myrafeeq.api.repository.UserRepository;
import uz.myrafeeq.api.security.JwtTokenProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramAuthService {

  private static final String HMAC_SHA256 = "HmacSHA256";

  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserMapper userMapper;
  private final ObjectMapper objectMapper;
  private final TelegramProperties telegramProperties;

  @Transactional
  public AuthResponse authenticate(TelegramAuthRequest request) {
    log.debug("Authenticating initData (length={})", request.getInitData().length());

    Map<String, String> params = parseInitData(request.getInitData());
    log.debug("Parsed initData keys: {}", params.keySet());

    verifyHmac(params);
    verifyAuthDate(params);

    JsonNode userNode = parseUserJson(params.get("user"));

    JsonNode idNode = userNode.get("id");
    if (idNode == null || !idNode.isNumber()) {
      throw new InvalidAuthException("Missing or invalid user ID in Telegram data");
    }

    Long telegramId = idNode.asLong();
    String firstName = userNode.has("first_name") ? userNode.get("first_name").asString() : "";
    String username = userNode.has("username") ? userNode.get("username").asString() : null;
    String languageCode =
        userNode.has("language_code") ? userNode.get("language_code").asString() : "en";

    UserEntity user = upsertUser(telegramId, firstName, username, languageCode);

    String token = jwtTokenProvider.generateToken(telegramId, firstName);
    UserResponse userResponse = userMapper.toUserResponse(user);

    return AuthResponse.builder().token(token).user(userResponse).build();
  }

  private Map<String, String> parseInitData(String initData) {
    try {
      return Arrays.stream(initData.split("&"))
          .map(pair -> pair.split("=", 2))
          .filter(parts -> parts.length == 2)
          .collect(
              Collectors.toMap(
                  parts -> URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                  parts -> URLDecoder.decode(parts[1], StandardCharsets.UTF_8)));
    } catch (Exception _) {
      throw new InvalidAuthException("Failed to parse init data");
    }
  }

  private void verifyHmac(Map<String, String> params) {
    String receivedHash = params.get("hash");
    if (receivedHash == null || receivedHash.isBlank()) {
      log.warn("Missing hash in init data. Available keys: {}", params.keySet());
      throw new InvalidAuthException("Missing hash in init data");
    }

    String dataCheckString =
        params.entrySet().stream()
            .filter(e -> !"hash".equals(e.getKey()) && !"signature".equals(e.getKey()))
            .sorted(Map.Entry.comparingByKey())
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("\n"));

    try {
      byte[] secretKey =
          hmacSha256(
              "WebAppData".getBytes(StandardCharsets.UTF_8), telegramProperties.getBotToken());
      byte[] hash = hmacSha256(secretKey, dataCheckString);
      String computedHash = bytesToHex(hash);

      if (!MessageDigest.isEqual(
          computedHash.getBytes(StandardCharsets.UTF_8),
          receivedHash.getBytes(StandardCharsets.UTF_8))) {
        log.warn(
            "HMAC mismatch: computed={}, received={}, dataCheckString keys={}",
            computedHash.substring(0, 8) + "...",
            receivedHash.substring(0, Math.min(8, receivedHash.length())) + "...",
            params.keySet().stream()
                .filter(k -> !"hash".equals(k) && !"signature".equals(k))
                .sorted()
                .toList());
        throw new InvalidAuthException("Invalid HMAC signature");
      }
    } catch (InvalidAuthException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to verify HMAC signature", e);
      throw new InvalidAuthException("Failed to verify HMAC signature");
    }
  }

  private void verifyAuthDate(Map<String, String> params) {
    String authDateStr = params.get("auth_date");
    if (authDateStr == null || authDateStr.isBlank()) {
      throw new InvalidAuthException("Missing auth_date in init data");
    }

    try {
      long authDateEpoch = Long.parseLong(authDateStr);
      Instant authDate = Instant.ofEpochSecond(authDateEpoch);
      Instant now = Instant.now();
      Instant cutoff = now.minus(telegramProperties.getAuthDataTtl());

      if (authDate.isBefore(cutoff)) {
        log.warn(
            "Init data expired: authDate={}, now={}, ttl={}, age={}s",
            authDate,
            now,
            telegramProperties.getAuthDataTtl(),
            now.getEpochSecond() - authDate.getEpochSecond());
        throw new InvalidAuthException("Init data has expired");
      }
    } catch (NumberFormatException e) {
      log.warn("Invalid auth_date format: '{}'", authDateStr);
      throw new InvalidAuthException("Invalid auth_date format");
    }
  }

  private byte[] hmacSha256(byte[] key, String data) {
    try {
      Mac mac = Mac.getInstance(HMAC_SHA256);
      mac.init(new SecretKeySpec(key, HMAC_SHA256));
      return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new InvalidAuthException("HMAC computation failed");
    }
  }

  private String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  private JsonNode parseUserJson(String userJson) {
    if (userJson == null || userJson.isBlank()) {
      throw new InvalidAuthException("Missing user data in init data");
    }
    try {
      return objectMapper.readTree(userJson);
    } catch (JacksonException e) {
      throw new InvalidAuthException("Failed to parse user JSON");
    }
  }

  private UserEntity upsertUser(
      Long telegramId, String firstName, String username, String languageCode) {
    return userRepository
        .findById(telegramId)
        .map(
            existing -> {
              existing.setFirstName(firstName);
              existing.setUsername(username);
              existing.setLanguageCode(languageCode);
              return userRepository.save(existing);
            })
        .orElseGet(
            () ->
                userRepository.save(
                    UserEntity.builder()
                        .telegramId(telegramId)
                        .firstName(firstName)
                        .username(username)
                        .languageCode(languageCode)
                        .build()));
  }
}
