package uz.myrafeeq.api.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.myrafeeq.api.dto.request.OnboardingRequest;
import uz.myrafeeq.api.dto.response.CityResponse;
import uz.myrafeeq.api.dto.response.OnboardingResponse;
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
import uz.myrafeeq.api.exception.UserNotFoundException;
import uz.myrafeeq.api.mapper.CityMapper;
import uz.myrafeeq.api.mapper.PreferencesMapper;
import uz.myrafeeq.api.mapper.UserMapper;
import uz.myrafeeq.api.repository.CityRepository;
import uz.myrafeeq.api.repository.UserPreferencesRepository;
import uz.myrafeeq.api.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class OnboardingService {

  private final UserRepository userRepository;
  private final UserPreferencesRepository preferencesRepository;
  private final CityRepository cityRepository;
  private final PreferencesMapper preferencesMapper;
  private final CityMapper cityMapper;
  private final UserMapper userMapper;

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
        city.getCountry() != null && city.getCountry().getDefaultMethod() != null
            ? city.getCountry().getDefaultMethod()
            : CalculationMethod.MWL;

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
            .prayerNotifications(request.prayerNotifications())
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
}
