package uz.myrafeeq.api.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.myrafeeq.api.dto.request.UpdatePreferencesRequest;
import uz.myrafeeq.api.dto.response.CityResponse;
import uz.myrafeeq.api.dto.response.UserPreferencesResponse;
import uz.myrafeeq.api.entity.UserPreferencesEntity;
import uz.myrafeeq.api.exception.CityNotFoundException;
import uz.myrafeeq.api.exception.PreferencesNotFoundException;
import uz.myrafeeq.api.mapper.CityMapper;
import uz.myrafeeq.api.mapper.PreferencesMapper;
import uz.myrafeeq.api.repository.CityRepository;
import uz.myrafeeq.api.repository.UserPreferencesRepository;

@Service
@RequiredArgsConstructor
public class UserPreferencesService {

  private final UserPreferencesRepository preferencesRepository;
  private final CityRepository cityRepository;
  private final PreferencesMapper preferencesMapper;
  private final CityMapper cityMapper;

  @Transactional(readOnly = true)
  public UserPreferencesResponse getPreferences(Long telegramId) {
    UserPreferencesEntity prefs =
        preferencesRepository
            .findById(telegramId)
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
            .findById(telegramId)
            .orElseThrow(
                () ->
                    new PreferencesNotFoundException(
                        "Preferences not found for user: " + telegramId));

    applyPartialUpdate(prefs, request);
    prefs = preferencesRepository.save(prefs);

    CityResponse city = resolveCity(prefs.getCityId());
    return preferencesMapper.toPreferencesResponse(prefs, city);
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
      prefs.setPrayerNotifications(request.prayerNotifications());
    }
    if (request.manualAdjustments() != null) {
      prefs.setManualAdjustments(request.manualAdjustments());
    }
  }

  private CityResponse resolveCity(String cityId) {
    if (cityId == null) {
      return null;
    }
    return cityRepository.findById(cityId).map(cityMapper::toCityResponse).orElse(null);
  }
}
