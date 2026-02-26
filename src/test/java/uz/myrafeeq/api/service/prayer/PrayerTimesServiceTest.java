package uz.myrafeeq.api.service.prayer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
  private static final double TASHKENT_LAT = 41.2995;
  private static final double TASHKENT_LON = 69.2401;

  @Mock private UserPreferencesRepository preferencesRepository;
  @Mock private CityRepository cityRepository;
  @Mock private PreferencesMapper preferencesMapper;
  @InjectMocks private PrayerTimesService prayerTimesService;

  @Test
  void should_returnPrayerTimes_when_userHasPreferences() {
    UserPreferencesEntity prefs = buildPreferences();
    CityEntity city = buildCity();

    given(preferencesRepository.findByTelegramId(TELEGRAM_ID)).willReturn(Optional.of(prefs));
    given(cityRepository.findById("tashkent")).willReturn(Optional.of(city));
    given(preferencesMapper.jsonToIntegerMap(null)).willReturn(Map.of());

    List<PrayerTimesResponse> result =
        prayerTimesService.calculatePrayerTimes(TELEGRAM_ID, LocalDate.of(2025, 3, 10), 1);

    assertThat(result).hasSize(1);
    PrayerTimesResponse response = result.getFirst();
    assertThat(response.date()).isEqualTo(LocalDate.of(2025, 3, 10));
    assertThat(response.times().fajr()).matches("\\d{2}:\\d{2}");
    assertThat(response.times().dhuhr()).matches("\\d{2}:\\d{2}");
    assertThat(response.times().asr()).matches("\\d{2}:\\d{2}");
    assertThat(response.times().maghrib()).matches("\\d{2}:\\d{2}");
    assertThat(response.times().isha()).matches("\\d{2}:\\d{2}");
    assertThat(response.meta().calculationMethod()).isEqualTo("MBOUZ");
    assertThat(response.meta().madhab()).isEqualTo("HANAFI");
  }

  @Test
  void should_returnMultipleDays_when_daysGreaterThanOne() {
    UserPreferencesEntity prefs = buildPreferences();
    CityEntity city = buildCity();

    given(preferencesRepository.findByTelegramId(TELEGRAM_ID)).willReturn(Optional.of(prefs));
    given(cityRepository.findById("tashkent")).willReturn(Optional.of(city));
    given(preferencesMapper.jsonToIntegerMap(null)).willReturn(Map.of());

    List<PrayerTimesResponse> result =
        prayerTimesService.calculatePrayerTimes(TELEGRAM_ID, LocalDate.of(2025, 3, 10), 3);

    assertThat(result).hasSize(3);
    assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2025, 3, 10));
    assertThat(result.get(1).date()).isEqualTo(LocalDate.of(2025, 3, 11));
    assertThat(result.get(2).date()).isEqualTo(LocalDate.of(2025, 3, 12));
  }

  @Test
  void should_throwPreferencesNotFound_when_noPreferences() {
    given(preferencesRepository.findByTelegramId(TELEGRAM_ID)).willReturn(Optional.empty());

    assertThatThrownBy(
            () -> prayerTimesService.calculatePrayerTimes(TELEGRAM_ID, LocalDate.now(), 1))
        .isInstanceOf(PreferencesNotFoundException.class)
        .hasMessageContaining(TELEGRAM_ID.toString());
  }

  @Test
  void should_returnPrayerTimesByLocation_when_publicEndpoint() {
    PrayerTimesResponse result =
        prayerTimesService.calculatePrayerTimesByLocation(
            TASHKENT_LAT,
            TASHKENT_LON,
            LocalDate.of(2025, 6, 15),
            CalculationMethod.MWL,
            "Asia/Tashkent",
            Madhab.HANAFI);

    assertThat(result.date()).isEqualTo(LocalDate.of(2025, 6, 15));
    assertThat(result.times().fajr()).matches("\\d{2}:\\d{2}");
    assertThat(result.meta().calculationMethod()).isEqualTo("MWL");
    assertThat(result.meta().madhab()).isEqualTo("HANAFI");
  }

  @Test
  void should_useDefaults_when_optionalParamsNull() {
    PrayerTimesResponse result =
        prayerTimesService.calculatePrayerTimesByLocation(
            TASHKENT_LAT, TASHKENT_LON, null, null, null, null);

    assertThat(result.date()).isEqualTo(LocalDate.now());
    assertThat(result.meta().calculationMethod()).isEqualTo("MWL");
    assertThat(result.meta().madhab()).isEqualTo("SHAFI");
  }

  @ParameterizedTest
  @EnumSource(CalculationMethod.class)
  void should_returnValidTimes_when_anyCalculationMethod(CalculationMethod method) {
    PrayerTimesResponse result =
        prayerTimesService.calculatePrayerTimesByLocation(
            TASHKENT_LAT,
            TASHKENT_LON,
            LocalDate.of(2025, 3, 21),
            method,
            "Asia/Tashkent",
            Madhab.HANAFI);

    assertThat(result.times().fajr()).isNotEqualTo("--:--");
    assertThat(result.times().dhuhr()).isNotEqualTo("--:--");
    assertThat(result.times().asr()).isNotEqualTo("--:--");
    assertThat(result.times().maghrib()).isNotEqualTo("--:--");
    assertThat(result.times().isha()).isNotEqualTo("--:--");
  }

  @Test
  void should_includeHijriDate_when_calculatingTimes() {
    PrayerTimesResponse result =
        prayerTimesService.calculatePrayerTimesByLocation(
            TASHKENT_LAT,
            TASHKENT_LON,
            LocalDate.of(2025, 3, 10),
            CalculationMethod.MWL,
            "Asia/Tashkent",
            Madhab.SHAFI);

    assertThat(result.hijriDate()).isNotBlank();
    assertThat(result.hijriDate()).matches("\\d+ .+ \\d+");
  }

  @Test
  void should_handleInvalidTimezone_when_fallbackToUtc() {
    PrayerTimesResponse result =
        prayerTimesService.calculatePrayerTimesByLocation(
            TASHKENT_LAT,
            TASHKENT_LON,
            LocalDate.of(2025, 3, 10),
            CalculationMethod.MWL,
            "Invalid/Timezone",
            Madhab.SHAFI);

    assertThat(result).isNotNull();
    assertThat(result.times().fajr()).matches("\\d{2}:\\d{2}");
  }

  @Test
  void should_includeAdjustments_when_preferencesHaveAdjustments() {
    UserPreferencesEntity prefs = buildPreferences();
    prefs.setManualAdjustments("{\"FAJR\":2,\"ISHA\":-3}");
    CityEntity city = buildCity();

    given(preferencesRepository.findByTelegramId(TELEGRAM_ID)).willReturn(Optional.of(prefs));
    given(cityRepository.findById("tashkent")).willReturn(Optional.of(city));
    given(preferencesMapper.jsonToIntegerMap("{\"FAJR\":2,\"ISHA\":-3}"))
        .willReturn(Map.of("FAJR", 2, "ISHA", -3));

    List<PrayerTimesResponse> result =
        prayerTimesService.calculatePrayerTimes(TELEGRAM_ID, LocalDate.of(2025, 3, 10), 1);

    assertThat(result.getFirst().meta().adjustments()).isNotNull();
    assertThat(result.getFirst().meta().adjustments()).containsEntry("FAJR", 2);
  }

  @Test
  void should_handleNullCity_when_preferencesHaveNoCity() {
    UserPreferencesEntity prefs = buildPreferences();
    prefs.setCityId(null);

    given(preferencesRepository.findByTelegramId(TELEGRAM_ID)).willReturn(Optional.of(prefs));
    given(preferencesMapper.jsonToIntegerMap(null)).willReturn(Map.of());

    List<PrayerTimesResponse> result =
        prayerTimesService.calculatePrayerTimes(TELEGRAM_ID, LocalDate.of(2025, 3, 10), 1);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().city()).isEqualTo("Unknown");
  }

  private UserPreferencesEntity buildPreferences() {
    return UserPreferencesEntity.builder()
        .telegramId(TELEGRAM_ID)
        .cityId("tashkent")
        .latitude(TASHKENT_LAT)
        .longitude(TASHKENT_LON)
        .calculationMethod(CalculationMethod.MBOUZ)
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
        .latitude(TASHKENT_LAT)
        .longitude(TASHKENT_LON)
        .timezone("Asia/Tashkent")
        .build();
  }
}
