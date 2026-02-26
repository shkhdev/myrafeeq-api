package uz.myrafeeq.api.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

    log.info("Preferences updated for user={}", telegramId);

    CityResponse city = resolveCity(prefs.getCityId());
    return preferencesMapper.toPreferencesResponse(prefs, city);
  }

  private void applyPartialUpdate(UserPreferencesEntity prefs, UpdatePreferencesRequest request) {
    if (request.getCityId() != null) {
      cityRepository
          .findById(request.getCityId())
          .orElseThrow(() -> new CityNotFoundException("City not found: " + request.getCityId()));
      prefs.setCityId(request.getCityId());
    }
    if (request.getCalculationMethod() != null) {
      prefs.setCalculationMethod(request.getCalculationMethod());
    }
    if (request.getMadhab() != null) {
      prefs.setMadhab(request.getMadhab());
    }
    if (request.getHighLatitudeRule() != null) {
      prefs.setHighLatitudeRule(request.getHighLatitudeRule());
    }
    if (request.getHijriCorrection() != null) {
      prefs.setHijriCorrection(request.getHijriCorrection());
    }
    if (request.getTimeFormat() != null) {
      prefs.setTimeFormat(request.getTimeFormat());
    }
    if (request.getTheme() != null) {
      prefs.setTheme(request.getTheme());
    }
    if (request.getNotificationsEnabled() != null) {
      prefs.setNotificationsEnabled(request.getNotificationsEnabled());
    }
    if (request.getReminderTiming() != null) {
      prefs.setReminderTiming(request.getReminderTiming());
    }
    if (request.getPrayerNotifications() != null) {
      prefs.setPrayerNotifications(request.getPrayerNotifications());
    }
    if (request.getManualAdjustments() != null) {
      prefs.setManualAdjustments(request.getManualAdjustments());
    }
  }

  private CityResponse resolveCity(String cityId) {
    if (cityId == null) {
      return null;
    }
    return cityRepository.findById(cityId).map(cityMapper::toCityResponse).orElse(null);
  }
}
