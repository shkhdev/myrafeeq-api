package uz.myrafeeq.api.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.myrafeeq.api.dto.request.OnboardingRequest;
import uz.myrafeeq.api.dto.response.CityResponse;
import uz.myrafeeq.api.dto.response.OnboardingResponse;
import uz.myrafeeq.api.dto.response.UserPreferencesResponse;
import uz.myrafeeq.api.dto.response.UserResponse;
import uz.myrafeeq.api.entity.CityEntity;
import uz.myrafeeq.api.entity.CountryEntity;
import uz.myrafeeq.api.entity.UserEntity;
import uz.myrafeeq.api.entity.UserPreferencesEntity;
import uz.myrafeeq.api.enums.CalculationMethod;
import uz.myrafeeq.api.enums.Madhab;
import uz.myrafeeq.api.enums.ReminderTiming;
import uz.myrafeeq.api.exception.CityNotFoundException;
import uz.myrafeeq.api.exception.OnboardingAlreadyCompletedException;
import uz.myrafeeq.api.exception.UserNotFoundException;
import uz.myrafeeq.api.mapper.CityMapper;
import uz.myrafeeq.api.mapper.PreferencesMapper;
import uz.myrafeeq.api.mapper.UserMapper;
import uz.myrafeeq.api.repository.CityRepository;
import uz.myrafeeq.api.repository.UserPreferencesRepository;
import uz.myrafeeq.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

  private static final Long TELEGRAM_ID = 123456789L;

  @Mock private UserRepository userRepository;
  @Mock private UserPreferencesRepository preferencesRepository;
  @Mock private CityRepository cityRepository;
  @Mock private PreferencesMapper preferencesMapper;
  @Mock private CityMapper cityMapper;
  @Mock private UserMapper userMapper;
  @InjectMocks private OnboardingService onboardingService;

  @Test
  void should_completeOnboarding_when_validRequest() {
    UserEntity user = buildUserEntity(false);
    CityEntity city = buildCityEntity();
    CityResponse cityResponse = buildCityResponse();
    UserResponse userResponse = buildUserResponse();
    UserPreferencesResponse prefsResponse = buildPreferencesResponse();

    OnboardingRequest request =
        new OnboardingRequest("tashkent", 41.2995, 69.2401, true, Map.of(), ReminderTiming.ON_TIME);

    given(userRepository.findById(TELEGRAM_ID)).willReturn(Optional.of(user));
    given(cityRepository.findById("tashkent")).willReturn(Optional.of(city));
    given(preferencesRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
    given(userRepository.save(any())).willReturn(user);
    given(cityMapper.toCityResponse(city)).willReturn(cityResponse);
    given(userMapper.toUserResponse(any())).willReturn(userResponse);
    given(preferencesMapper.toPreferencesResponse(any(), any())).willReturn(prefsResponse);

    OnboardingResponse result = onboardingService.completeOnboarding(TELEGRAM_ID, request);

    assertThat(result.user()).isNotNull();
    assertThat(result.preferences()).isNotNull();
  }

  @Test
  void should_throwUserNotFound_when_onboardingForNonExistingUser() {
    OnboardingRequest request = new OnboardingRequest("tashkent", null, null, true, null, null);

    given(userRepository.findById(TELEGRAM_ID)).willReturn(Optional.empty());

    assertThatThrownBy(() -> onboardingService.completeOnboarding(TELEGRAM_ID, request))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void should_throwOnboardingAlreadyCompleted_when_alreadyOnboarded() {
    UserEntity user = buildUserEntity(true);
    OnboardingRequest request = new OnboardingRequest("tashkent", null, null, true, null, null);

    given(userRepository.findById(TELEGRAM_ID)).willReturn(Optional.of(user));

    assertThatThrownBy(() -> onboardingService.completeOnboarding(TELEGRAM_ID, request))
        .isInstanceOf(OnboardingAlreadyCompletedException.class);
  }

  @Test
  void should_throwCityNotFound_when_onboardingWithInvalidCity() {
    UserEntity user = buildUserEntity(false);
    OnboardingRequest request = new OnboardingRequest("nonexistent", null, null, true, null, null);

    given(userRepository.findById(TELEGRAM_ID)).willReturn(Optional.of(user));
    given(cityRepository.findById("nonexistent")).willReturn(Optional.empty());

    assertThatThrownBy(() -> onboardingService.completeOnboarding(TELEGRAM_ID, request))
        .isInstanceOf(CityNotFoundException.class);
  }

  @Test
  void should_useCountryDefaults_when_onboardingWithCountryMethod() {
    UserEntity user = buildUserEntity(false);
    CityEntity city = buildCityEntity();
    CityResponse cityResponse = buildCityResponse();

    OnboardingRequest request = new OnboardingRequest("tashkent", null, null, true, null, null);

    given(userRepository.findById(TELEGRAM_ID)).willReturn(Optional.of(user));
    given(cityRepository.findById("tashkent")).willReturn(Optional.of(city));
    given(preferencesRepository.save(any()))
        .willAnswer(
            inv -> {
              UserPreferencesEntity saved = inv.getArgument(0);
              assertThat(saved.getCalculationMethod()).isEqualTo(CalculationMethod.MBOUZ);
              assertThat(saved.getLatitude()).isEqualTo(city.getLatitude());
              assertThat(saved.getLongitude()).isEqualTo(city.getLongitude());
              return saved;
            });
    given(userRepository.save(any())).willReturn(user);
    given(cityMapper.toCityResponse(city)).willReturn(cityResponse);
    given(userMapper.toUserResponse(any())).willReturn(buildUserResponse());
    given(preferencesMapper.toPreferencesResponse(any(), any()))
        .willReturn(buildPreferencesResponse());

    onboardingService.completeOnboarding(TELEGRAM_ID, request);
  }

  @Test
  void should_useCityCoordinates_when_onboardingWithoutExplicitLocation() {
    UserEntity user = buildUserEntity(false);
    CityEntity city = buildCityEntity();
    CityResponse cityResponse = buildCityResponse();

    OnboardingRequest request = new OnboardingRequest("tashkent", null, null, true, null, null);

    given(userRepository.findById(TELEGRAM_ID)).willReturn(Optional.of(user));
    given(cityRepository.findById("tashkent")).willReturn(Optional.of(city));
    given(preferencesRepository.save(any()))
        .willAnswer(
            inv -> {
              UserPreferencesEntity saved = inv.getArgument(0);
              assertThat(saved.getLatitude()).isEqualTo(41.2995);
              assertThat(saved.getLongitude()).isEqualTo(69.2401);
              return saved;
            });
    given(userRepository.save(any())).willReturn(user);
    given(cityMapper.toCityResponse(city)).willReturn(cityResponse);
    given(userMapper.toUserResponse(any())).willReturn(buildUserResponse());
    given(preferencesMapper.toPreferencesResponse(any(), any()))
        .willReturn(buildPreferencesResponse());

    onboardingService.completeOnboarding(TELEGRAM_ID, request);
  }

  private UserEntity buildUserEntity(boolean onboarded) {
    return UserEntity.builder()
        .telegramId(TELEGRAM_ID)
        .firstName("Doston")
        .username("doston")
        .languageCode("uz")
        .onboardingCompleted(onboarded)
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
        .id("tashkent")
        .name("Tashkent")
        .country(country)
        .latitude(41.2995)
        .longitude(69.2401)
        .timezone("Asia/Tashkent")
        .build();
  }

  private CityResponse buildCityResponse() {
    return CityResponse.builder()
        .id("tashkent")
        .name("Tashkent")
        .country("UZ")
        .latitude(41.2995)
        .longitude(69.2401)
        .timezone("Asia/Tashkent")
        .defaultMethod("MBOUZ")
        .defaultMadhab("HANAFI")
        .build();
  }

  private UserResponse buildUserResponse() {
    return UserResponse.builder()
        .telegramId(TELEGRAM_ID)
        .firstName("Doston")
        .languageCode("uz")
        .onboardingCompleted(false)
        .build();
  }

  private UserPreferencesResponse buildPreferencesResponse() {
    return UserPreferencesResponse.builder()
        .calculationMethod("MBOUZ")
        .madhab("HANAFI")
        .hijriCorrection(0)
        .build();
  }
}
