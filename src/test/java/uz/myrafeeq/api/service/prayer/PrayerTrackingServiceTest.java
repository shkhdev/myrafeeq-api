package uz.myrafeeq.api.service.prayer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.myrafeeq.api.dto.request.TogglePrayerRequest;
import uz.myrafeeq.api.dto.response.PrayerStatsResponse;
import uz.myrafeeq.api.dto.response.PrayerTrackingResponse;
import uz.myrafeeq.api.dto.response.TogglePrayerResponse;
import uz.myrafeeq.api.entity.PrayerTrackingEntity;
import uz.myrafeeq.api.enums.PrayerName;
import uz.myrafeeq.api.enums.StatsPeriod;
import uz.myrafeeq.api.exception.TrackingValidationException;
import uz.myrafeeq.api.mapper.PrayerTrackingMapper;
import uz.myrafeeq.api.repository.CityRepository;
import uz.myrafeeq.api.repository.PrayerTrackingRepository;
import uz.myrafeeq.api.repository.UserPreferencesRepository;
import uz.myrafeeq.api.repository.projection.DateCountProjection;
import uz.myrafeeq.api.repository.projection.PrayerCountProjection;

@ExtendWith(MockitoExtension.class)
class PrayerTrackingServiceTest {

  private static final Long TELEGRAM_ID = 123456789L;

  @Mock private PrayerTrackingRepository trackingRepository;
  @Mock private PrayerTrackingMapper trackingMapper;
  @Mock private UserPreferencesRepository preferencesRepository;
  @Mock private CityRepository cityRepository;
  @InjectMocks private PrayerTrackingService trackingService;

  @Test
  void should_returnTrackingByDate_when_dateProvided() {
    LocalDate today = LocalDate.now();
    PrayerTrackingResponse response = PrayerTrackingResponse.builder().tracking(Map.of()).build();

    given(trackingRepository.findByTelegramIdAndPrayerDate(TELEGRAM_ID, today))
        .willReturn(List.of());
    given(trackingMapper.toTrackingResponse(any())).willReturn(response);

    PrayerTrackingResponse result = trackingService.getTracking(TELEGRAM_ID, today, null, null);

    assertThat(result.getTracking()).isEmpty();
  }

  @Test
  void should_returnTrackingByRange_when_fromAndToProvided() {
    LocalDate from = LocalDate.now().minusDays(7);
    LocalDate to = LocalDate.now();
    PrayerTrackingResponse response = PrayerTrackingResponse.builder().tracking(Map.of()).build();

    given(trackingRepository.findByTelegramIdAndPrayerDateBetween(TELEGRAM_ID, from, to))
        .willReturn(List.of());
    given(trackingMapper.toTrackingResponse(any())).willReturn(response);

    PrayerTrackingResponse result = trackingService.getTracking(TELEGRAM_ID, null, from, to);

    assertThat(result).isNotNull();
  }

  @Test
  void should_defaultToToday_when_noDateParams() {
    PrayerTrackingResponse response = PrayerTrackingResponse.builder().tracking(Map.of()).build();

    given(trackingRepository.findByTelegramIdAndPrayerDate(eq(TELEGRAM_ID), any(LocalDate.class)))
        .willReturn(List.of());
    given(trackingMapper.toTrackingResponse(any())).willReturn(response);

    PrayerTrackingResponse result = trackingService.getTracking(TELEGRAM_ID, null, null, null);

    assertThat(result).isNotNull();
  }

  @Test
  void should_createNewTracking_when_toggleNewPrayer() {
    LocalDate today = LocalDate.now();
    TogglePrayerRequest request = new TogglePrayerRequest(today, PrayerName.FAJR, true);

    given(
            trackingRepository.findByTelegramIdAndPrayerDateAndPrayerName(
                TELEGRAM_ID, today, PrayerName.FAJR))
        .willReturn(Optional.empty());

    PrayerTrackingEntity saved =
        PrayerTrackingEntity.builder()
            .id(UUID.randomUUID())
            .telegramId(TELEGRAM_ID)
            .prayerDate(today)
            .prayerName(PrayerName.FAJR)
            .prayed(true)
            .toggledAt(Instant.now())
            .build();
    given(trackingRepository.save(any(PrayerTrackingEntity.class))).willReturn(saved);

    TogglePrayerResponse result = trackingService.togglePrayer(TELEGRAM_ID, request);

    assertThat(result.getPrayer()).isEqualTo("FAJR");
    assertThat(result.getPrayed()).isTrue();
    assertThat(result.getDate()).isEqualTo(today);
  }

  @Test
  void should_updateExisting_when_toggleExistingPrayer() {
    LocalDate today = LocalDate.now();
    TogglePrayerRequest request = new TogglePrayerRequest(today, PrayerName.DHUHR, false);

    PrayerTrackingEntity existing =
        PrayerTrackingEntity.builder()
            .id(UUID.randomUUID())
            .telegramId(TELEGRAM_ID)
            .prayerDate(today)
            .prayerName(PrayerName.DHUHR)
            .prayed(true)
            .toggledAt(Instant.now().minusSeconds(60))
            .build();

    given(
            trackingRepository.findByTelegramIdAndPrayerDateAndPrayerName(
                TELEGRAM_ID, today, PrayerName.DHUHR))
        .willReturn(Optional.of(existing));
    given(trackingRepository.save(any(PrayerTrackingEntity.class)))
        .willAnswer(inv -> inv.getArgument(0));

    TogglePrayerResponse result = trackingService.togglePrayer(TELEGRAM_ID, request);

    assertThat(result.getPrayed()).isFalse();
  }

  @Test
  void should_throwValidationError_when_futureDate() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    TogglePrayerRequest request = new TogglePrayerRequest(tomorrow, PrayerName.FAJR, true);

    assertThatThrownBy(() -> trackingService.togglePrayer(TELEGRAM_ID, request))
        .isInstanceOf(TrackingValidationException.class)
        .hasMessageContaining("future dates");
  }

  @Test
  void should_throwValidationError_when_dateOlderThan7Days() {
    LocalDate tooOld = LocalDate.now().minusDays(8);
    TogglePrayerRequest request = new TogglePrayerRequest(tooOld, PrayerName.FAJR, true);

    assertThatThrownBy(() -> trackingService.togglePrayer(TELEGRAM_ID, request))
        .isInstanceOf(TrackingValidationException.class)
        .hasMessageContaining("older than 7 days");
  }

  @Test
  void should_allowTracking_when_exactlySevenDaysAgo() {
    LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
    TogglePrayerRequest request = new TogglePrayerRequest(sevenDaysAgo, PrayerName.ASR, true);

    given(
            trackingRepository.findByTelegramIdAndPrayerDateAndPrayerName(
                TELEGRAM_ID, sevenDaysAgo, PrayerName.ASR))
        .willReturn(Optional.empty());

    PrayerTrackingEntity saved =
        PrayerTrackingEntity.builder()
            .id(UUID.randomUUID())
            .telegramId(TELEGRAM_ID)
            .prayerDate(sevenDaysAgo)
            .prayerName(PrayerName.ASR)
            .prayed(true)
            .toggledAt(Instant.now())
            .build();
    given(trackingRepository.save(any())).willReturn(saved);

    TogglePrayerResponse result = trackingService.togglePrayer(TELEGRAM_ID, request);

    assertThat(result.getDate()).isEqualTo(sevenDaysAgo);
  }

  @Test
  void should_allowTracking_when_todaysDate() {
    LocalDate today = LocalDate.now();
    TogglePrayerRequest request = new TogglePrayerRequest(today, PrayerName.MAGHRIB, true);

    given(
            trackingRepository.findByTelegramIdAndPrayerDateAndPrayerName(
                TELEGRAM_ID, today, PrayerName.MAGHRIB))
        .willReturn(Optional.empty());

    PrayerTrackingEntity saved =
        PrayerTrackingEntity.builder()
            .id(UUID.randomUUID())
            .telegramId(TELEGRAM_ID)
            .prayerDate(today)
            .prayerName(PrayerName.MAGHRIB)
            .prayed(true)
            .toggledAt(Instant.now())
            .build();
    given(trackingRepository.save(any())).willReturn(saved);

    TogglePrayerResponse result = trackingService.togglePrayer(TELEGRAM_ID, request);

    assertThat(result.getDate()).isEqualTo(today);
    assertThat(result.getPrayer()).isEqualTo("MAGHRIB");
  }

  @ParameterizedTest
  @EnumSource(StatsPeriod.class)
  void should_returnStats_when_anyPeriod(StatsPeriod period) {
    given(
            trackingRepository.countCompletedByPrayer(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(List.of());
    given(
            trackingRepository.countCompletedByDate(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(List.of());

    PrayerStatsResponse result = trackingService.getStats(TELEGRAM_ID, period);

    assertThat(result.getPeriod()).isEqualTo(period.name());
    assertThat(result.getTotal()).isPositive();
    assertThat(result.getCompleted()).isZero();
    assertThat(result.getPercentage()).isZero();
    assertThat(result.getStreak()).isZero();
    assertThat(result.getByPrayer()).hasSize(PrayerName.values().length);
  }

  @Test
  void should_calculateStreak_when_consecutiveDaysCompleted() {
    LocalDate today = LocalDate.now();

    given(
            trackingRepository.countCompletedByPrayer(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(List.of());
    given(
            trackingRepository.countCompletedByDate(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(
            List.of(
                dateCount(today, 5L),
                dateCount(today.minusDays(1), 5L),
                dateCount(today.minusDays(2), 5L)));

    PrayerStatsResponse result = trackingService.getStats(TELEGRAM_ID, StatsPeriod.WEEK);

    assertThat(result.getStreak()).isEqualTo(3);
  }

  @Test
  void should_breakStreak_when_dayMissed() {
    LocalDate today = LocalDate.now();

    given(
            trackingRepository.countCompletedByPrayer(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(List.of());
    given(
            trackingRepository.countCompletedByDate(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(
            List.of(
                dateCount(today, 5L),
                dateCount(today.minusDays(1), 5L),
                dateCount(today.minusDays(3), 5L)));

    PrayerStatsResponse result = trackingService.getStats(TELEGRAM_ID, StatsPeriod.WEEK);

    assertThat(result.getStreak()).isEqualTo(2);
  }

  @Test
  void should_calculatePercentage_when_someCompleted() {
    LocalDate today = LocalDate.now();

    given(
            trackingRepository.countCompletedByPrayer(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(
            List.of(
                prayerCount(PrayerName.FAJR, 1L),
                prayerCount(PrayerName.DHUHR, 1L),
                prayerCount(PrayerName.ASR, 1L),
                prayerCount(PrayerName.MAGHRIB, 1L),
                prayerCount(PrayerName.ISHA, 1L)));
    given(
            trackingRepository.countCompletedByDate(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(List.of(dateCount(today, 5L)));

    PrayerStatsResponse result = trackingService.getStats(TELEGRAM_ID, StatsPeriod.WEEK);

    assertThat(result.getCompleted()).isEqualTo(5);
    assertThat(result.getPercentage()).isPositive();
  }

  @Test
  void should_returnZeroStreak_when_noEntries() {
    given(
            trackingRepository.countCompletedByPrayer(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(List.of());
    given(
            trackingRepository.countCompletedByDate(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(List.of());

    PrayerStatsResponse result = trackingService.getStats(TELEGRAM_ID, StatsPeriod.WEEK);

    assertThat(result.getStreak()).isZero();
  }

  @Test
  void should_countAllPrayerTypes_when_buildingStats() {
    given(
            trackingRepository.countCompletedByPrayer(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(List.of());
    given(
            trackingRepository.countCompletedByDate(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(List.of());

    PrayerStatsResponse result = trackingService.getStats(TELEGRAM_ID, StatsPeriod.WEEK);

    assertThat(result.getByPrayer()).containsKeys("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA");
  }

  private PrayerCountProjection prayerCount(PrayerName prayer, Long count) {
    return new PrayerCountProjection() {
      @Override
      public PrayerName getPrayerName() {
        return prayer;
      }

      @Override
      public Long getCount() {
        return count;
      }
    };
  }

  private DateCountProjection dateCount(LocalDate date, Long count) {
    return new DateCountProjection() {
      @Override
      public LocalDate getPrayerDate() {
        return date;
      }

      @Override
      public Long getCount() {
        return count;
      }
    };
  }
}
