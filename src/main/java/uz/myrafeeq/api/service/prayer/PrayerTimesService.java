package uz.myrafeeq.api.service.prayer;

import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.data.DateComponents;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.myrafeeq.api.dto.response.PrayerTimesResponse;
import uz.myrafeeq.api.entity.CityEntity;
import uz.myrafeeq.api.entity.UserPreferencesEntity;
import uz.myrafeeq.api.enums.CalculationMethod;
import uz.myrafeeq.api.enums.HighLatitudeRule;
import uz.myrafeeq.api.enums.Madhab;
import uz.myrafeeq.api.exception.PreferencesNotFoundException;
import uz.myrafeeq.api.mapper.PreferencesMapper;
import uz.myrafeeq.api.repository.CityRepository;
import uz.myrafeeq.api.repository.UserPreferencesRepository;

@Service
@RequiredArgsConstructor
public class PrayerTimesService {

  private final UserPreferencesRepository preferencesRepository;
  private final CityRepository cityRepository;
  private final PreferencesMapper preferencesMapper;

  @Transactional(readOnly = true)
  public List<PrayerTimesResponse> calculatePrayerTimes(Long telegramId, LocalDate date, int days) {
    UserPreferencesEntity prefs =
        preferencesRepository
            .findByTelegramId(telegramId)
            .orElseThrow(
                () ->
                    new PreferencesNotFoundException(
                        "Preferences not found for user: " + telegramId));

    CityEntity city =
        prefs.getCityId() != null ? cityRepository.findById(prefs.getCityId()).orElse(null) : null;

    PrayerCalculationParams params =
        PrayerCalculationParams.fromPreferences(prefs, city, preferencesMapper);

    LocalDate startDate = date != null ? date : LocalDate.now();

    List<PrayerTimesResponse> results = new ArrayList<>();
    for (int i = 0; i < days; i++) {
      results.add(computePrayerTimes(startDate.plusDays(i), params));
    }
    return results;
  }

  public PrayerTimesResponse calculatePrayerTimesByLocation(
      double lat, double lon, LocalDate date, CalculationMethod method, String timezone,
      Madhab madhab) {
    LocalDate targetDate = date != null ? date : LocalDate.now();
    CalculationMethod calcMethod = method != null ? method : CalculationMethod.MWL;

    PrayerCalculationParams params =
        new PrayerCalculationParams(
            lat,
            lon,
            calcMethod,
            madhab != null ? madhab : Madhab.SHAFI,
            HighLatitudeRule.MIDDLE_OF_NIGHT,
            timezone != null ? timezone : "UTC",
            Map.of(),
            0,
            "");

    return computePrayerTimes(targetDate, params);
  }

  private PrayerTimesResponse computePrayerTimes(LocalDate date, PrayerCalculationParams params) {
    Coordinates coordinates = new Coordinates(params.latitude(), params.longitude());
    DateComponents dateComponents =
        new DateComponents(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    CalculationParameters calcParams = toAdhanParams(params);

    PrayerTimes prayerTimes = new PrayerTimes(coordinates, dateComponents, calcParams);

    ZoneId zoneId;
    try {
      zoneId = ZoneId.of(params.timezone());
    } catch (Exception _) {
      zoneId = ZoneOffset.UTC;
    }

    return PrayerTimesResponse.builder()
        .date(date.toString())
        .hijriDate(HijriDateCalculator.toHijriDate(date, params.hijriCorrection()))
        .city(params.cityName())
        .times(
            PrayerTimesResponse.PrayerTimesDto.builder()
                .fajr(formatTime(prayerTimes.fajr, zoneId))
                .sunrise(formatTime(prayerTimes.sunrise, zoneId))
                .dhuhr(formatTime(prayerTimes.dhuhr, zoneId))
                .asr(formatTime(prayerTimes.asr, zoneId))
                .maghrib(formatTime(prayerTimes.maghrib, zoneId))
                .isha(formatTime(prayerTimes.isha, zoneId))
                .build())
        .meta(
            PrayerTimesResponse.PrayerTimesMeta.builder()
                .calculationMethod(params.method().name())
                .madhab(params.madhab().name())
                .adjustments(params.adjustments().isEmpty() ? null : params.adjustments())
                .build())
        .build();
  }

  private CalculationParameters toAdhanParams(PrayerCalculationParams params) {
    CalculationParameters calcParams = params.method().getParameters();
    calcParams.madhab = params.madhab().toAdhan();
    calcParams.highLatitudeRule = params.highLatitudeRule().toAdhan();

    Map<String, Integer> adj = params.adjustments();
    if (!adj.isEmpty()) {
      calcParams.adjustments.fajr = adj.getOrDefault("FAJR", 0);
      calcParams.adjustments.dhuhr = adj.getOrDefault("DHUHR", 0);
      calcParams.adjustments.asr = adj.getOrDefault("ASR", 0);
      calcParams.adjustments.maghrib = adj.getOrDefault("MAGHRIB", 0);
      calcParams.adjustments.isha = adj.getOrDefault("ISHA", 0);
    }

    return calcParams;
  }

  private String formatTime(Date time, ZoneId zoneId) {
    if (time == null) {
      return "--:--";
    }
    ZonedDateTime zdt = time.toInstant().atZone(zoneId);
    return String.format("%02d:%02d", zdt.getHour(), zdt.getMinute());
  }

  record PrayerCalculationParams(
      double latitude,
      double longitude,
      CalculationMethod method,
      Madhab madhab,
      HighLatitudeRule highLatitudeRule,
      String timezone,
      Map<String, Integer> adjustments,
      int hijriCorrection,
      String cityName) {

    static PrayerCalculationParams fromPreferences(
        UserPreferencesEntity prefs, CityEntity city, PreferencesMapper mapper) {
      return new PrayerCalculationParams(
          prefs.getLatitude() != null ? prefs.getLatitude() : 0.0,
          prefs.getLongitude() != null ? prefs.getLongitude() : 0.0,
          prefs.getCalculationMethod() != null
              ? prefs.getCalculationMethod()
              : CalculationMethod.MWL,
          prefs.getMadhab() != null ? prefs.getMadhab() : Madhab.SHAFI,
          prefs.getHighLatitudeRule() != null
              ? prefs.getHighLatitudeRule()
              : HighLatitudeRule.MIDDLE_OF_NIGHT,
          city != null ? city.getTimezone() : "UTC",
          mapper.jsonToIntegerMap(prefs.getManualAdjustments()),
          prefs.getHijriCorrection() != null ? prefs.getHijriCorrection() : 0,
          city != null ? city.getName() : "Unknown");
    }
  }
}
