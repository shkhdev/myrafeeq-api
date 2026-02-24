package uz.myrafeeq.api.service.user;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uz.myrafeeq.api.dto.request.OnboardingRequest;
import uz.myrafeeq.api.dto.request.UpdatePreferencesRequest;
import uz.myrafeeq.api.dto.response.CityResponse;
import uz.myrafeeq.api.dto.response.OnboardingResponse;
import uz.myrafeeq.api.dto.response.UserPreferencesResponse;
import uz.myrafeeq.api.entity.CityEntity;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPreferencesService {

  private final UserRepository userRepository;
  private final UserPreferencesRepository preferencesRepository;
  private final CityRepository cityRepository;
  private final PreferencesMapper preferencesMapper;
  private final CityMapper cityMapper;
  private final UserMapper userMapper;
  private final ObjectMapper objectMapper;

  @Transactional(readOnly = true)
  public UserPreferencesResponse getPreferences(Long telegramId) {
    UserPreferencesEntity prefs =
        preferencesRepository
            .findByTelegramId(telegramId)
            .orElseThrow(
                () ->
                    new PreferencesNotFoundException(
                        "Preferences not found for user: " + telegramId));

    CityResponse city = resolveCity(prefs.getCityId());
    return preferencesMapper.toPreferencesResponse(prefs, city);
  }

  @Transactional
  public UserPreferencesResponse updatePreferences(
      Long telegramId, UpdatePreferencesRequest request) {
    UserPreferencesEntity prefs =
        preferencesRepository
            .findByTelegramId(telegramId)
            .orElseThrow(
                () ->
                    new PreferencesNotFoundException(
                        "Preferences not found for user: " + telegramId));

    applyPartialUpdate(prefs, request);
    prefs = preferencesRepository.save(prefs);

    CityResponse city = resolveCity(prefs.getCityId());
    return preferencesMapper.toPreferencesResponse(prefs, city);
  }

  @Transactional
  public OnboardingResponse completeOnboarding(Long telegramId, OnboardingRequest request) {
    UserEntity user =
        userRepository
            .findById(telegramId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + telegramId));

    if (Boolean.TRUE.equals(user.getOnboardingCompleted())) {
      throw new OnboardingAlreadyCompletedException(
          "Onboarding already completed for user: " + telegramId);
    }

    CityEntity city =
        cityRepository
            .findById(request.cityId())
            .orElseThrow(() -> new CityNotFoundException("City not found: " + request.cityId()));

    CalculationMethod method =
        city.getRecommendedMethod() != null ? city.getRecommendedMethod() : CalculationMethod.MWL;

    UserPreferencesEntity prefs =
        UserPreferencesEntity.builder()
            .telegramId(telegramId)
            .cityId(city.getId())
            .latitude(request.latitude() != null ? request.latitude() : city.getLatitude())
            .longitude(request.longitude() != null ? request.longitude() : city.getLongitude())
            .calculationMethod(method)
            .madhab(Madhab.HANAFI)
            .highLatitudeRule(HighLatitudeRule.MIDDLE_OF_NIGHT)
            .timeFormat(TimeFormat.TWENTY_FOUR_HOUR)
            .theme(ThemePreference.SYSTEM)
            .notificationsEnabled(request.notificationsEnabled())
            .reminderTiming(
                request.reminderTiming() != null
                    ? request.reminderTiming()
                    : ReminderTiming.ON_TIME)
            .prayerNotifications(toJson(request.prayerNotifications()))
            .build();

    prefs = preferencesRepository.save(prefs);

    user.setOnboardingCompleted(true);
    user = userRepository.save(user);

    CityResponse cityResponse = cityMapper.toCityResponse(city);
    return OnboardingResponse.builder()
        .user(userMapper.toUserResponse(user))
        .preferences(preferencesMapper.toPreferencesResponse(prefs, cityResponse))
        .build();
  }

  private void applyPartialUpdate(UserPreferencesEntity prefs, UpdatePreferencesRequest request) {
    if (request.cityId() != null) {
      cityRepository
          .findById(request.cityId())
          .orElseThrow(() -> new CityNotFoundException("City not found: " + request.cityId()));
      prefs.setCityId(request.cityId());
    }
    if (request.calculationMethod() != null) {
      prefs.setCalculationMethod(request.calculationMethod());
    }
    if (request.madhab() != null) {
      prefs.setMadhab(request.madhab());
    }
    if (request.highLatitudeRule() != null) {
      prefs.setHighLatitudeRule(request.highLatitudeRule());
    }
    if (request.hijriCorrection() != null) {
      prefs.setHijriCorrection(request.hijriCorrection());
    }
    if (request.timeFormat() != null) {
      prefs.setTimeFormat(request.timeFormat());
    }
    if (request.theme() != null) {
      prefs.setTheme(request.theme());
    }
    if (request.notificationsEnabled() != null) {
      prefs.setNotificationsEnabled(request.notificationsEnabled());
    }
    if (request.reminderTiming() != null) {
      prefs.setReminderTiming(request.reminderTiming());
    }
    if (request.prayerNotifications() != null) {
      prefs.setPrayerNotifications(toJson(request.prayerNotifications()));
    }
    if (request.manualAdjustments() != null) {
      prefs.setManualAdjustments(toJson(request.manualAdjustments()));
    }
  }

  private CityResponse resolveCity(String cityId) {
    if (cityId == null) {
      return null;
    }
    return cityRepository.findById(cityId).map(cityMapper::toCityResponse).orElse(null);
  }

  private String toJson(Map<String, ?> map) {
    if (map == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(map);
    } catch (JacksonException e) {
      log.warn("Failed to serialize map to JSON: {}", e.getMessage());
      return null;
    }
  }
}
