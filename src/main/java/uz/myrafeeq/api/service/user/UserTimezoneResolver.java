package uz.myrafeeq.api.service.user;

import java.time.ZoneId;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uz.myrafeeq.api.repository.CityRepository;
import uz.myrafeeq.api.repository.UserPreferencesRepository;

@Component
@RequiredArgsConstructor
public class UserTimezoneResolver {

  private final UserPreferencesRepository preferencesRepository;
  private final CityRepository cityRepository;

  @Cacheable(value = "userTimezone", key = "#telegramId")
  public ZoneId resolveTimezone(Long telegramId) {
    return preferencesRepository
        .findById(telegramId)
        .filter(prefs -> prefs.getCityId() != null)
        .flatMap(prefs -> cityRepository.findById(prefs.getCityId()))
        .map(
            city -> {
              try {
                return ZoneId.of(city.getTimezone());
              } catch (Exception _) {
                return (ZoneId) ZoneOffset.UTC;
              }
            })
        .orElse(ZoneOffset.UTC);
  }
}
