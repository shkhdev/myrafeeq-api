package uz.myrafeeq.api.service.prayer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.myrafeeq.api.dto.response.PrayerTimesResponse;
import uz.myrafeeq.api.entity.CityEntity;
import uz.myrafeeq.api.entity.CountryEntity;
import uz.myrafeeq.api.entity.UserPreferencesEntity;
import uz.myrafeeq.api.enums.CalculationMethod;
import uz.myrafeeq.api.enums.HighLatitudeRule;
import uz.myrafeeq.api.enums.Madhab;
import uz.myrafeeq.api.exception.PreferencesNotFoundException;
import uz.myrafeeq.api.mapper.PreferencesMapper;
import uz.myrafeeq.api.repository.CityRepository;
import uz.myrafeeq.api.repository.UserPreferencesRepository;

@ExtendWith(MockitoExtension.class)
class PrayerTimesServiceTest {

  private static final Long TELEGRAM_ID = 123456789L;
  @Mock private UserPreferencesRepository preferencesRepository;
  @Mock private CityRepository cityRepository;
  @Mock private PreferencesMapper preferencesMapper;
  @InjectMocks private PrayerTimesService prayerTimesService;

  private UserPreferencesEntity buildPreferences() {
    return UserPreferencesEntity.builder()
        .id(UUID.randomUUID())
        .telegramId(TELEGRAM_ID)
        .cityId("tashkent")
        .latitude(41.2995)
        .longitude(69.2401)
        .calculationMethod(CalculationMethod.MWL)
        .madhab(Madhab.HANAFI)
        .highLatitudeRule(HighLatitudeRule.MIDDLE_OF_NIGHT)
        .hijriCorrection(0)
        .build();
  }

  private CityEntity buildCity() {
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

  @Test
  void calculatePrayerTimesReturnsCorrectDayCount() {
    UserPreferencesEntity prefs = buildPreferences();
    CityEntity city = buildCity();
    LocalDate date = LocalDate.of(2026, 2, 24);

    when(preferencesRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(prefs));
    when(cityRepository.findById("tashkent")).thenReturn(Optional.of(city));
    when(preferencesMapper.jsonToIntegerMap(any())).thenReturn(Map.of());

    List<PrayerTimesResponse> result =
        prayerTimesService.calculatePrayerTimes(TELEGRAM_ID, date, 3);

    assertThat(result).hasSize(3);
    assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2026, 2, 24));
    assertThat(result.get(1).date()).isEqualTo(LocalDate.of(2026, 2, 25));
    assertThat(result.get(2).date()).isEqualTo(LocalDate.of(2026, 2, 26));
  }

  @Test
  void calculatePrayerTimesDefaultsToToday() {
    UserPreferencesEntity prefs = buildPreferences();
    CityEntity city = buildCity();

    when(preferencesRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(prefs));
    when(cityRepository.findById("tashkent")).thenReturn(Optional.of(city));
    when(preferencesMapper.jsonToIntegerMap(any())).thenReturn(Map.of());

    List<PrayerTimesResponse> result =
        prayerTimesService.calculatePrayerTimes(TELEGRAM_ID, null, 1);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().date()).isEqualTo(LocalDate.now());
  }

  @Test
  void calculatePrayerTimesThrowsWhenNoPrefs() {
    when(preferencesRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                prayerTimesService.calculatePrayerTimes(TELEGRAM_ID, LocalDate.of(2026, 2, 24), 1))
        .isInstanceOf(PreferencesNotFoundException.class)
        .hasMessageContaining("Preferences not found for user");
  }

  @Test
  void calculatePrayerTimesByLocationDefaultsMWL() {
    LocalDate date = LocalDate.of(2026, 6, 15);

    PrayerTimesResponse result =
        prayerTimesService.calculatePrayerTimesByLocation(41.2995, 69.2401, date, null, null, null);

    assertThat(result).isNotNull();
    assertThat(result.meta().calculationMethod()).isEqualTo("MWL");
    assertThat(result.meta().madhab()).isEqualTo("SHAFI");
    assertThat(result.date()).isEqualTo(LocalDate.of(2026, 6, 15));
  }

  @Test
  void calculatePrayerTimesByLocationReturnsFormattedTimes() {
    LocalDate date = LocalDate.of(2026, 3, 21);

    PrayerTimesResponse result =
        prayerTimesService.calculatePrayerTimesByLocation(
            41.2995, 69.2401, date, CalculationMethod.MWL, null, null);

    assertThat(result.times()).isNotNull();
    assertThat(result.times().fajr()).matches("\\d{2}:\\d{2}");
    assertThat(result.times().sunrise()).matches("\\d{2}:\\d{2}");
    assertThat(result.times().dhuhr()).matches("\\d{2}:\\d{2}");
    assertThat(result.times().asr()).matches("\\d{2}:\\d{2}");
    assertThat(result.times().maghrib()).matches("\\d{2}:\\d{2}");
    assertThat(result.times().isha()).matches("\\d{2}:\\d{2}");
  }

  @Test
  void calculatePrayerTimesUsesManualAdjustments() {
    UserPreferencesEntity prefs = buildPreferences();
    prefs.setManualAdjustments("{\"FAJR\":2,\"ISHA\":-3}");
    CityEntity city = buildCity();

    when(preferencesRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(prefs));
    when(cityRepository.findById("tashkent")).thenReturn(Optional.of(city));
    when(preferencesMapper.jsonToIntegerMap("{\"FAJR\":2,\"ISHA\":-3}"))
        .thenReturn(Map.of("FAJR", 2, "ISHA", -3));

    List<PrayerTimesResponse> result =
        prayerTimesService.calculatePrayerTimes(TELEGRAM_ID, LocalDate.of(2026, 3, 21), 1);

    assertThat(result).hasSize(1);
    PrayerTimesResponse response = result.getFirst();
    assertThat(response.meta().adjustments()).isNotNull();
    assertThat(response.meta().adjustments()).containsEntry("FAJR", 2);
    assertThat(response.meta().adjustments()).containsEntry("ISHA", -3);
  }
}
