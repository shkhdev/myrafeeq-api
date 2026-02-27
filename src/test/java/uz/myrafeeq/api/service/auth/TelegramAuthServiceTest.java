package uz.myrafeeq.api.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import tools.jackson.databind.ObjectMapper;
import uz.myrafeeq.api.configuration.TelegramProperties;
import uz.myrafeeq.api.dto.request.TelegramAuthRequest;
import uz.myrafeeq.api.dto.response.AuthResponse;
import uz.myrafeeq.api.entity.UserEntity;
import uz.myrafeeq.api.exception.InvalidAuthException;
import uz.myrafeeq.api.mapper.UserMapper;
import uz.myrafeeq.api.repository.UserRepository;
import uz.myrafeeq.api.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class TelegramAuthServiceTest {

  private static final String BOT_TOKEN = "7819384521:AAH-test-bot-token-for-unit-tests";
  private static final Long USER_TELEGRAM_ID = 123456789L;

  @Mock private UserRepository userRepository;
  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private UserMapper userMapper;
  @Mock private Environment environment;

  private TelegramAuthService authService;

  @BeforeEach
  void setUp() {
    TelegramProperties telegramProperties =
        new TelegramProperties(BOT_TOKEN, Duration.ofMinutes(5));
    ObjectMapper objectMapper = new ObjectMapper();
    authService =
        new TelegramAuthService(
            userRepository,
            jwtTokenProvider,
            userMapper,
            objectMapper,
            telegramProperties,
            environment);
  }

  @Test
  void should_authenticateSuccessfully_when_validInitData() {
    String initData = buildValidInitData();

    given(userRepository.findById(USER_TELEGRAM_ID)).willReturn(Optional.empty());
    given(userRepository.save(any(UserEntity.class))).willAnswer(inv -> inv.getArgument(0));
    given(jwtTokenProvider.generateToken(eq(USER_TELEGRAM_ID), any())).willReturn("jwt-token");
    given(userMapper.toUserResponse(any())).willReturn(null);

    AuthResponse response = authService.authenticate(new TelegramAuthRequest(initData));

    assertThat(response.getToken()).isEqualTo("jwt-token");
  }

  @Test
  void should_createNewUser_when_firstLogin() {
    String initData = buildValidInitData();

    given(userRepository.findById(USER_TELEGRAM_ID)).willReturn(Optional.empty());
    given(userRepository.save(any(UserEntity.class))).willAnswer(inv -> inv.getArgument(0));
    given(jwtTokenProvider.generateToken(any(), any())).willReturn("token");
    given(userMapper.toUserResponse(any())).willReturn(null);

    authService.authenticate(new TelegramAuthRequest(initData));

    ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(captor.capture());
    UserEntity saved = captor.getValue();
    assertThat(saved.getTelegramId()).isEqualTo(USER_TELEGRAM_ID);
    assertThat(saved.getFirstName()).isEqualTo("Doston");
  }

  @Test
  void should_updateUser_when_dataChanged() {
    String initData = buildValidInitData();
    UserEntity existing =
        UserEntity.builder()
            .telegramId(USER_TELEGRAM_ID)
            .firstName("OldName")
            .username("doston")
            .languageCode("uz")
            .build();

    given(userRepository.findById(USER_TELEGRAM_ID)).willReturn(Optional.of(existing));
    given(userRepository.save(any(UserEntity.class))).willReturn(existing);
    given(jwtTokenProvider.generateToken(any(), any())).willReturn("token");
    given(userMapper.toUserResponse(any())).willReturn(null);

    authService.authenticate(new TelegramAuthRequest(initData));

    verify(userRepository).save(existing);
    assertThat(existing.getFirstName()).isEqualTo("Doston");
  }

  @Test
  void should_notUpdateUser_when_dataUnchanged() {
    String initData = buildValidInitData();
    UserEntity existing =
        UserEntity.builder()
            .telegramId(USER_TELEGRAM_ID)
            .firstName("Doston")
            .username("doston")
            .languageCode("uz")
            .build();

    given(userRepository.findById(USER_TELEGRAM_ID)).willReturn(Optional.of(existing));
    given(jwtTokenProvider.generateToken(any(), any())).willReturn("token");
    given(userMapper.toUserResponse(any())).willReturn(null);

    authService.authenticate(new TelegramAuthRequest(initData));

    verify(userRepository, never()).save(any());
  }

  @Test
  void should_throw_when_invalidHmacSignature() {
    String initData =
        "auth_date="
            + Instant.now().getEpochSecond()
            + "&user="
            + URLEncoder.encode(userJson(), StandardCharsets.UTF_8)
            + "&hash=0000000000000000000000000000000000000000000000000000000000000000";

    assertThatThrownBy(() -> authService.authenticate(new TelegramAuthRequest(initData)))
        .isInstanceOf(InvalidAuthException.class)
        .hasMessageContaining("HMAC");
  }

  @Test
  void should_throw_when_missingHash() {
    String initData =
        "auth_date="
            + Instant.now().getEpochSecond()
            + "&user="
            + URLEncoder.encode(userJson(), StandardCharsets.UTF_8);

    assertThatThrownBy(() -> authService.authenticate(new TelegramAuthRequest(initData)))
        .isInstanceOf(InvalidAuthException.class)
        .hasMessageContaining("hash");
  }

  @Test
  void should_throw_when_expiredAuthDate() {
    long expiredEpoch = Instant.now().minus(Duration.ofMinutes(10)).getEpochSecond();
    String initData = buildInitDataWithAuthDate(expiredEpoch);

    assertThatThrownBy(() -> authService.authenticate(new TelegramAuthRequest(initData)))
        .isInstanceOf(InvalidAuthException.class)
        .hasMessageContaining("expired");
  }

  @Test
  void should_throw_when_missingUserData() {
    long now = Instant.now().getEpochSecond();
    Map<String, String> params = new TreeMap<>();
    params.put("auth_date", String.valueOf(now));
    params.put("query_id", "AAH123");
    String dataCheckString =
        params.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("\n"));
    String hash = computeHmac(dataCheckString);
    String initData = "auth_date=" + now + "&query_id=AAH123&hash=" + hash;

    assertThatThrownBy(() -> authService.authenticate(new TelegramAuthRequest(initData)))
        .isInstanceOf(InvalidAuthException.class)
        .hasMessageContaining("user data");
  }

  @Test
  void should_throw_when_invalidUserJson() {
    long now = Instant.now().getEpochSecond();
    String badUserJson = "not-valid-json";
    Map<String, String> params = new TreeMap<>();
    params.put("auth_date", String.valueOf(now));
    params.put("user", badUserJson);
    String dataCheckString =
        params.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("\n"));
    String hash = computeHmac(dataCheckString);
    String initData =
        "auth_date="
            + now
            + "&user="
            + URLEncoder.encode(badUserJson, StandardCharsets.UTF_8)
            + "&hash="
            + hash;

    assertThatThrownBy(() -> authService.authenticate(new TelegramAuthRequest(initData)))
        .isInstanceOf(InvalidAuthException.class)
        .hasMessageContaining("parse user JSON");
  }

  @Test
  void should_throw_when_missingUserId() {
    long now = Instant.now().getEpochSecond();
    String userJsonNoId = "{\"first_name\":\"Doston\"}";
    Map<String, String> params = new TreeMap<>();
    params.put("auth_date", String.valueOf(now));
    params.put("user", userJsonNoId);
    String dataCheckString =
        params.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("\n"));
    String hash = computeHmac(dataCheckString);
    String initData =
        "auth_date="
            + now
            + "&user="
            + URLEncoder.encode(userJsonNoId, StandardCharsets.UTF_8)
            + "&hash="
            + hash;

    assertThatThrownBy(() -> authService.authenticate(new TelegramAuthRequest(initData)))
        .isInstanceOf(InvalidAuthException.class)
        .hasMessageContaining("user ID");
  }

  @Test
  void should_throw_when_missingAuthDate() {
    String userJsonStr = userJson();
    Map<String, String> params = new TreeMap<>();
    params.put("user", userJsonStr);
    String dataCheckString =
        params.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("\n"));
    String hash = computeHmac(dataCheckString);
    String initData =
        "user=" + URLEncoder.encode(userJsonStr, StandardCharsets.UTF_8) + "&hash=" + hash;

    assertThatThrownBy(() -> authService.authenticate(new TelegramAuthRequest(initData)))
        .isInstanceOf(InvalidAuthException.class)
        .hasMessageContaining("auth_date");
  }

  // --- helpers ---

  private String userJson() {
    return "{\"id\":"
        + USER_TELEGRAM_ID
        + ",\"first_name\":\"Doston\",\"username\":\"doston\",\"language_code\":\"uz\"}";
  }

  private String buildValidInitData() {
    return buildInitDataWithAuthDate(Instant.now().getEpochSecond());
  }

  private String buildInitDataWithAuthDate(long authDateEpoch) {
    String userJsonStr = userJson();

    Map<String, String> params = new TreeMap<>();
    params.put("auth_date", String.valueOf(authDateEpoch));
    params.put("user", userJsonStr);

    String dataCheckString =
        params.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("\n"));

    String hash = computeHmac(dataCheckString);

    return "auth_date="
        + authDateEpoch
        + "&user="
        + URLEncoder.encode(userJsonStr, StandardCharsets.UTF_8)
        + "&hash="
        + hash;
  }

  private String computeHmac(String dataCheckString) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec("WebAppData".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] secretKey = mac.doFinal(BOT_TOKEN.getBytes(StandardCharsets.UTF_8));

      Mac dataMac = Mac.getInstance("HmacSHA256");
      dataMac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
      byte[] hashBytes = dataMac.doFinal(dataCheckString.getBytes(StandardCharsets.UTF_8));

      StringBuilder sb = new StringBuilder(hashBytes.length * 2);
      for (byte b : hashBytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
