package uz.myrafeeq.api.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import uz.myrafeeq.api.dto.request.OnboardingRequest;
import uz.myrafeeq.api.dto.request.UpdatePreferencesRequest;
import uz.myrafeeq.api.dto.response.CityResponse;
import uz.myrafeeq.api.dto.response.OnboardingResponse;
import uz.myrafeeq.api.dto.response.UserPreferencesResponse;
import uz.myrafeeq.api.dto.response.UserResponse;
import uz.myrafeeq.api.entity.CityEntity;
import uz.myrafeeq.api.entity.CountryEntity;
import uz.myrafeeq.api.entity.UserEntity;
import uz.myrafeeq.api.entity.UserPreferencesEntity;
import uz.myrafeeq.api.enums.CalculationMethod;
import uz.myrafeeq.api.enums.HighLatitudeRule;
import uz.myrafeeq.api.enums.Madhab;
import uz.myrafeeq.api.enums.ReminderTiming;
import uz.myrafeeq.api.enums.ThemePreference;
import uz.myrafeeq.api.enums.TimeFormat;
import uz.myrafeeq.api.exception.CityNotFoundException;
import uz.myrafeeq.api.exception.OnboardingAlreadyCompletedException;
import uz.myrafeeq.api.exception.PreferencesNotFoundException;
import uz.myrafeeq.api.exception.UserNotFoundException;
import uz.myrafeeq.api.mapper.CityMapper;
import uz.myrafeeq.api.mapper.PreferencesMapper;
import uz.myrafeeq.api.mapper.UserMapper;
import uz.myrafeeq.api.repository.CityRepository;
import uz.myrafeeq.api.repository.UserPreferencesRepository;
import uz.myrafeeq.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserPreferencesServiceTest {

  private static final Long TELEGRAM_ID = 123456789L;
  private static final String CITY_ID = "tashkent";

  @Mock private UserRepository userRepository;
  @Mock private UserPreferencesRepository preferencesRepository;
  @Mock private CityRepository cityRepository;
  @Mock private PreferencesMapper preferencesMapper;
  @Mock private CityMapper cityMapper;
  @Mock private UserMapper userMapper;
  @Spy private ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks private UserPreferencesService service;

  @Test
  void getPreferencesReturnsResponse() {
    UserPreferencesEntity prefs = buildPreferencesEntity();
    CityEntity city = buildCityEntity();
    CityResponse cityResponse = buildCityResponse();
    UserPreferencesResponse expectedResponse = buildPreferencesResponse(cityResponse);

    when(preferencesRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(prefs));
    when(cityRepository.findById(CITY_ID)).thenReturn(Optional.of(city));
    when(cityMapper.toCityResponse(city)).thenReturn(cityResponse);
    when(preferencesMapper.toPreferencesResponse(prefs, cityResponse)).thenReturn(expectedResponse);

    UserPreferencesResponse result = service.getPreferences(TELEGRAM_ID);

    assertThat(result).isEqualTo(expectedResponse);
    verify(preferencesRepository).findByTelegramId(TELEGRAM_ID);
  }

  @Test
  void getPreferencesThrowsWhenNotFound() {
    when(preferencesRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getPreferences(TELEGRAM_ID))
        .isInstanceOf(PreferencesNotFoundException.class)
        .hasMessageContaining(String.valueOf(TELEGRAM_ID));
  }

  @Test
  void updatePreferencesAppliesPartialUpdate() {
    UserPreferencesEntity prefs = buildPreferencesEntity();
    CityEntity city = buildCityEntity();
    CityResponse cityResponse = buildCityResponse();
    UserPreferencesResponse expectedResponse = buildPreferencesResponse(cityResponse);

    UpdatePreferencesRequest request =
        new UpdatePreferencesRequest(
            null,
            CalculationMethod.ISNA,
            Madhab.STANDARD,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    when(preferencesRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(prefs));
    when(preferencesRepository.save(prefs)).thenReturn(prefs);
    when(cityRepository.findById(CITY_ID)).thenReturn(Optional.of(city));
    when(cityMapper.toCityResponse(city)).thenReturn(cityResponse);
    when(preferencesMapper.toPreferencesResponse(prefs, cityResponse)).thenReturn(expectedResponse);

    UserPreferencesResponse result = service.updatePreferences(TELEGRAM_ID, request);

    assertThat(result).isEqualTo(expectedResponse);
    assertThat(prefs.getCalculationMethod()).isEqualTo(CalculationMethod.ISNA);
    assertThat(prefs.getMadhab()).isEqualTo(Madhab.STANDARD);
    verify(preferencesRepository).save(prefs);
  }

  @Test
  void updatePreferencesThrowsWhenNotFound() {
    when(preferencesRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.empty());

    UpdatePreferencesRequest request =
        new UpdatePreferencesRequest(
            null, null, null, null, null, null, null, null, null, null, null);

    assertThatThrownBy(() -> service.updatePreferences(TELEGRAM_ID, request))
        .isInstanceOf(PreferencesNotFoundException.class)
        .hasMessageContaining(String.valueOf(TELEGRAM_ID));
  }

  @Test
  void completeOnboardingSuccess() {
    UserEntity user = buildUserEntity(false);
    CityEntity city = buildCityEntity();
    CityResponse cityResponse = buildCityResponse();
    UserResponse userResponse = buildUserResponse(true);
    UserPreferencesResponse prefsResponse = buildPreferencesResponse(cityResponse);

    OnboardingRequest request = new OnboardingRequest(CITY_ID, 41.2995, 69.2401, true, null, null);

    when(userRepository.findById(TELEGRAM_ID)).thenReturn(Optional.of(user));
    when(cityRepository.findById(CITY_ID)).thenReturn(Optional.of(city));
    when(preferencesRepository.save(any(UserPreferencesEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(userRepository.save(any(UserEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(cityMapper.toCityResponse(city)).thenReturn(cityResponse);
    when(userMapper.toUserResponse(any(UserEntity.class))).thenReturn(userResponse);
    when(preferencesMapper.toPreferencesResponse(
            any(UserPreferencesEntity.class), eq(cityResponse)))
        .thenReturn(prefsResponse);

    OnboardingResponse result = service.completeOnboarding(TELEGRAM_ID, request);

    assertThat(result).isNotNull();
    assertThat(result.user()).isEqualTo(userResponse);
    assertThat(result.preferences()).isEqualTo(prefsResponse);
    assertThat(user.getOnboardingCompleted()).isTrue();
    verify(preferencesRepository).save(any(UserPreferencesEntity.class));
    verify(userRepository).save(user);
  }

  @Test
  void completeOnboardingThrowsWhenUserNotFound() {
    when(userRepository.findById(TELEGRAM_ID)).thenReturn(Optional.empty());

    OnboardingRequest request = new OnboardingRequest(CITY_ID, 41.2995, 69.2401, true, null, null);

    assertThatThrownBy(() -> service.completeOnboarding(TELEGRAM_ID, request))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining(String.valueOf(TELEGRAM_ID));
  }

  @Test
  void completeOnboardingThrowsWhenAlreadyCompleted() {
    UserEntity user = buildUserEntity(true);

    when(userRepository.findById(TELEGRAM_ID)).thenReturn(Optional.of(user));

    OnboardingRequest request = new OnboardingRequest(CITY_ID, 41.2995, 69.2401, true, null, null);

    assertThatThrownBy(() -> service.completeOnboarding(TELEGRAM_ID, request))
        .isInstanceOf(OnboardingAlreadyCompletedException.class)
        .hasMessageContaining(String.valueOf(TELEGRAM_ID));
  }

  @Test
  void completeOnboardingThrowsWhenCityNotFound() {
    UserEntity user = buildUserEntity(false);

    when(userRepository.findById(TELEGRAM_ID)).thenReturn(Optional.of(user));
    when(cityRepository.findById(CITY_ID)).thenReturn(Optional.empty());

    OnboardingRequest request = new OnboardingRequest(CITY_ID, 41.2995, 69.2401, true, null, null);

    assertThatThrownBy(() -> service.completeOnboarding(TELEGRAM_ID, request))
        .isInstanceOf(CityNotFoundException.class)
        .hasMessageContaining(CITY_ID);
  }

  private UserPreferencesEntity buildPreferencesEntity() {
    return UserPreferencesEntity.builder()
        .id(UUID.randomUUID())
        .telegramId(TELEGRAM_ID)
        .cityId(CITY_ID)
        .latitude(41.2995)
        .longitude(69.2401)
        .calculationMethod(CalculationMethod.MWL)
        .madhab(Madhab.HANAFI)
        .highLatitudeRule(HighLatitudeRule.MIDDLE_OF_NIGHT)
        .timeFormat(TimeFormat.TWENTY_FOUR_HOUR)
        .theme(ThemePreference.SYSTEM)
        .notificationsEnabled(true)
        .reminderTiming(ReminderTiming.ON_TIME)
        .build();
  }

  private CityEntity buildCityEntity() {
    CountryEntity country =
        CountryEntity.builder()
            .code("UZ")
            .name("Uzbekistan")
            .defaultMethod(CalculationMethod.MBOUZ)
            .defaultMadhab(Madhab.HANAFI)
            .build();
    return CityEntity.builder()
        .id(CITY_ID)
        .name("Tashkent")
        .country(country)
        .latitude(41.2995)
        .longitude(69.2401)
        .timezone("Asia/Tashkent")
        .build();
  }

  private CityResponse buildCityResponse() {
    return CityResponse.builder()
        .id(CITY_ID)
        .name("Tashkent")
        .country("UZ")
        .latitude(41.2995)
        .longitude(69.2401)
        .timezone("Asia/Tashkent")
        .defaultMethod("MBOUZ")
        .defaultMadhab("HANAFI")
        .build();
  }

  private UserPreferencesResponse buildPreferencesResponse(CityResponse cityResponse) {
    return UserPreferencesResponse.builder()
        .city(cityResponse)
        .calculationMethod("MWL")
        .madhab("HANAFI")
        .highLatitudeRule("MIDDLE_OF_NIGHT")
        .hijriCorrection(0)
        .timeFormat("24h")
        .theme("SYSTEM")
        .notificationsEnabled(true)
        .reminderTiming("ON_TIME")
        .build();
  }

  private UserEntity buildUserEntity(boolean onboardingCompleted) {
    return UserEntity.builder()
        .telegramId(TELEGRAM_ID)
        .firstName("Doston")
        .username("doston")
        .languageCode("uz")
        .onboardingCompleted(onboardingCompleted)
        .build();
  }

  private UserResponse buildUserResponse(boolean onboardingCompleted) {
    return UserResponse.builder()
        .telegramId(TELEGRAM_ID)
        .firstName("Doston")
        .languageCode("uz")
        .onboardingCompleted(onboardingCompleted)
        .build();
  }
}
