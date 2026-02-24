package uz.myrafeeq.api.service.prayer;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
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

  private static final double SUNRISE_ANGLE = 0.833;

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
      double lat, double lon, LocalDate date, CalculationMethod method) {
    LocalDate targetDate = date != null ? date : LocalDate.now();
    CalculationMethod calcMethod = method != null ? method : CalculationMethod.MWL;

    PrayerCalculationParams params =
        new PrayerCalculationParams(
            lat,
            lon,
            calcMethod,
            Madhab.STANDARD,
            HighLatitudeRule.MIDDLE_OF_NIGHT,
            "UTC",
            Map.of(),
            "");

    return computePrayerTimes(targetDate, params);
  }

  private PrayerTimesResponse computePrayerTimes(LocalDate date, PrayerCalculationParams params) {
    CalculationMethodConfig config = CalculationMethodConfig.forMethod(params.method());
    double jd = SolarCalculator.julianDate(date);

    ZoneId zoneId;
    try {
      zoneId = ZoneId.of(params.timezone());
    } catch (Exception _) {
      zoneId = ZoneOffset.UTC;
    }

    int offsetSeconds =
        zoneId
            .getRules()
            .getOffset(date.atStartOfDay().toInstant(ZoneOffset.UTC))
            .getTotalSeconds();
    double utcOffsetHours = offsetSeconds / 3600.0;

    double fajrUtc =
        SolarCalculator.timeForAngle(
            jd, params.latitude(), params.longitude(), config.fajrAngle(), false);
    double sunriseUtc =
        SolarCalculator.timeForAngle(
            jd, params.latitude(), params.longitude(), SUNRISE_ANGLE, false);
    double dhuhrUtc = SolarCalculator.solarNoon(jd, params.longitude());
    int shadowRatio = params.madhab() == Madhab.HANAFI ? 2 : 1;
    double asrUtc = SolarCalculator.asrTime(jd, params.latitude(), params.longitude(), shadowRatio);
    double maghribUtc =
        SolarCalculator.timeForAngle(
            jd, params.latitude(), params.longitude(), SUNRISE_ANGLE, true);

    double ishaUtc;
    if (config.isIshaFixedOffset()) {
      ishaUtc = maghribUtc + config.ishaOffsetMinutes() / 60.0;
    } else {
      ishaUtc =
          SolarCalculator.timeForAngle(
              jd, params.latitude(), params.longitude(), config.ishaAngle(), true);
    }

    if (Double.isNaN(fajrUtc) || Double.isNaN(ishaUtc)) {
      double nightDuration = computeNightDuration(sunriseUtc, maghribUtc);
      if (Double.isNaN(fajrUtc)) {
        fajrUtc =
            applyHighLatitudeRule(
                params.highLatitudeRule(), sunriseUtc, nightDuration, config.fajrAngle(), false);
      }
      if (Double.isNaN(ishaUtc)) {
        ishaUtc =
            applyHighLatitudeRule(
                params.highLatitudeRule(), maghribUtc, nightDuration, config.ishaAngle(), true);
      }
    }

    Map<String, Integer> adjustments = params.adjustments();
    String fajr = formatTime(fajrUtc, utcOffsetHours, getAdjustment(adjustments, "FAJR"));
    String sunrise = formatTime(sunriseUtc, utcOffsetHours, 0);
    String dhuhr = formatTime(dhuhrUtc, utcOffsetHours, getAdjustment(adjustments, "DHUHR"));
    String asr = formatTime(asrUtc, utcOffsetHours, getAdjustment(adjustments, "ASR"));
    String maghrib = formatTime(maghribUtc, utcOffsetHours, getAdjustment(adjustments, "MAGHRIB"));
    String isha = formatTime(ishaUtc, utcOffsetHours, getAdjustment(adjustments, "ISHA"));

    return PrayerTimesResponse.builder()
        .date(date.toString())
        .hijriDate(HijriDateCalculator.toHijriDate(date))
        .city(params.cityName())
        .times(
            PrayerTimesResponse.PrayerTimesDto.builder()
                .fajr(fajr)
                .sunrise(sunrise)
                .dhuhr(dhuhr)
                .asr(asr)
                .maghrib(maghrib)
                .isha(isha)
                .build())
        .meta(
            PrayerTimesResponse.PrayerTimesMeta.builder()
                .calculationMethod(params.method().name())
                .madhab(params.madhab().name())
                .adjustments(adjustments.isEmpty() ? null : adjustments)
                .build())
        .build();
  }

  private double computeNightDuration(double sunriseUtc, double maghribUtc) {
    if (Double.isNaN(sunriseUtc) || Double.isNaN(maghribUtc)) {
      return 12.0;
    }
    return 24.0 - (maghribUtc - sunriseUtc);
  }

  private double applyHighLatitudeRule(
      HighLatitudeRule rule,
      double baseTime,
      double nightDuration,
      double angle,
      boolean afterNoon) {
    double portion =
        switch (rule) {
          case MIDDLE_OF_NIGHT -> 0.5;
          case ONE_SEVENTH -> 1.0 / 7.0;
          case ANGLE_BASED -> angle / 60.0;
        };

    double adjustmentHours = portion * nightDuration;
    return afterNoon ? baseTime + adjustmentHours : baseTime - adjustmentHours;
  }

  private int getAdjustment(Map<String, Integer> adjustments, String prayer) {
    Integer adj = adjustments.get(prayer);
    return adj != null ? adj : 0;
  }

  private String formatTime(double utcHours, double utcOffsetHours, int adjustmentMinutes) {
    if (Double.isNaN(utcHours)) {
      return "--:--";
    }
    double localHours = utcHours + utcOffsetHours + adjustmentMinutes / 60.0;
    localHours = ((localHours % 24.0) + 24.0) % 24.0;
    int hours = (int) localHours;
    int minutes = (int) Math.round((localHours - hours) * 60.0);
    if (minutes == 60) {
      hours += 1;
      minutes = 0;
    }
    hours = hours % 24;
    return String.format("%02d:%02d", hours, minutes);
  }

  record PrayerCalculationParams(
      double latitude,
      double longitude,
      CalculationMethod method,
      Madhab madhab,
      HighLatitudeRule highLatitudeRule,
      String timezone,
      Map<String, Integer> adjustments,
      String cityName) {

    static PrayerCalculationParams fromPreferences(
        UserPreferencesEntity prefs, CityEntity city, PreferencesMapper mapper) {
      return new PrayerCalculationParams(
          prefs.getLatitude() != null ? prefs.getLatitude() : 0.0,
          prefs.getLongitude() != null ? prefs.getLongitude() : 0.0,
          prefs.getCalculationMethod() != null
              ? prefs.getCalculationMethod()
              : CalculationMethod.MWL,
          prefs.getMadhab() != null ? prefs.getMadhab() : Madhab.STANDARD,
          prefs.getHighLatitudeRule() != null
              ? prefs.getHighLatitudeRule()
              : HighLatitudeRule.MIDDLE_OF_NIGHT,
          city != null ? city.getTimezone() : "UTC",
          mapper.jsonToIntegerMap(prefs.getManualAdjustments()),
          city != null ? city.getNameEn() : "Unknown");
    }
  }
}
