package uz.myrafeeq.api.service.prayer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
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
import uz.myrafeeq.api.repository.PrayerTrackingRepository;

@ExtendWith(MockitoExtension.class)
class PrayerTrackingServiceTest {

  private static final Long TELEGRAM_ID = 123456789L;

  @Mock private PrayerTrackingRepository trackingRepository;
  @Mock private PrayerTrackingMapper trackingMapper;
  @InjectMocks private PrayerTrackingService trackingService;

  @Test
  void should_returnTrackingByDate_when_dateProvided() {
    LocalDate today = LocalDate.now();
    PrayerTrackingResponse response = PrayerTrackingResponse.builder().tracking(Map.of()).build();

    given(trackingRepository.findByTelegramIdAndDate(TELEGRAM_ID, today)).willReturn(List.of());
    given(trackingMapper.toTrackingResponse(any())).willReturn(response);

    PrayerTrackingResponse result = trackingService.getTracking(TELEGRAM_ID, today, null, null);

    assertThat(result.tracking()).isEmpty();
  }

  @Test
  void should_returnTrackingByRange_when_fromAndToProvided() {
    LocalDate from = LocalDate.now().minusDays(7);
    LocalDate to = LocalDate.now();
    PrayerTrackingResponse response = PrayerTrackingResponse.builder().tracking(Map.of()).build();

    given(trackingRepository.findByTelegramIdAndDateBetween(TELEGRAM_ID, from, to))
        .willReturn(List.of());
    given(trackingMapper.toTrackingResponse(any())).willReturn(response);

    PrayerTrackingResponse result = trackingService.getTracking(TELEGRAM_ID, null, from, to);

    assertThat(result).isNotNull();
  }

  @Test
  void should_defaultToToday_when_noDateParams() {
    PrayerTrackingResponse response = PrayerTrackingResponse.builder().tracking(Map.of()).build();

    given(trackingRepository.findByTelegramIdAndDate(eq(TELEGRAM_ID), any(LocalDate.class)))
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
            trackingRepository.findByTelegramIdAndDateAndPrayerName(
                TELEGRAM_ID, today, PrayerName.FAJR))
        .willReturn(Optional.empty());

    PrayerTrackingEntity saved =
        PrayerTrackingEntity.builder()
            .id(UUID.randomUUID())
            .telegramId(TELEGRAM_ID)
            .date(today)
            .prayerName(PrayerName.FAJR)
            .prayed(true)
            .toggledAt(Instant.now())
            .build();
    given(trackingRepository.save(any(PrayerTrackingEntity.class))).willReturn(saved);

    TogglePrayerResponse result = trackingService.togglePrayer(TELEGRAM_ID, request);

    assertThat(result.prayer()).isEqualTo("FAJR");
    assertThat(result.prayed()).isTrue();
    assertThat(result.date()).isEqualTo(today);
  }

  @Test
  void should_updateExisting_when_toggleExistingPrayer() {
    LocalDate today = LocalDate.now();
    TogglePrayerRequest request = new TogglePrayerRequest(today, PrayerName.DHUHR, false);

    PrayerTrackingEntity existing =
        PrayerTrackingEntity.builder()
            .id(UUID.randomUUID())
            .telegramId(TELEGRAM_ID)
            .date(today)
            .prayerName(PrayerName.DHUHR)
            .prayed(true)
            .toggledAt(Instant.now().minusSeconds(60))
            .build();

    given(
            trackingRepository.findByTelegramIdAndDateAndPrayerName(
                TELEGRAM_ID, today, PrayerName.DHUHR))
        .willReturn(Optional.of(existing));
    given(trackingRepository.save(any(PrayerTrackingEntity.class)))
        .willAnswer(inv -> inv.getArgument(0));

    TogglePrayerResponse result = trackingService.togglePrayer(TELEGRAM_ID, request);

    assertThat(result.prayed()).isFalse();
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
            trackingRepository.findByTelegramIdAndDateAndPrayerName(
                TELEGRAM_ID, sevenDaysAgo, PrayerName.ASR))
        .willReturn(Optional.empty());

    PrayerTrackingEntity saved =
        PrayerTrackingEntity.builder()
            .id(UUID.randomUUID())
            .telegramId(TELEGRAM_ID)
            .date(sevenDaysAgo)
            .prayerName(PrayerName.ASR)
            .prayed(true)
            .toggledAt(Instant.now())
            .build();
    given(trackingRepository.save(any())).willReturn(saved);

    TogglePrayerResponse result = trackingService.togglePrayer(TELEGRAM_ID, request);

    assertThat(result.date()).isEqualTo(sevenDaysAgo);
  }

  @Test
  void should_allowTracking_when_todaysDate() {
    LocalDate today = LocalDate.now();
    TogglePrayerRequest request = new TogglePrayerRequest(today, PrayerName.MAGHRIB, true);

    given(
            trackingRepository.findByTelegramIdAndDateAndPrayerName(
                TELEGRAM_ID, today, PrayerName.MAGHRIB))
        .willReturn(Optional.empty());

    PrayerTrackingEntity saved =
        PrayerTrackingEntity.builder()
            .id(UUID.randomUUID())
            .telegramId(TELEGRAM_ID)
            .date(today)
            .prayerName(PrayerName.MAGHRIB)
            .prayed(true)
            .toggledAt(Instant.now())
            .build();
    given(trackingRepository.save(any())).willReturn(saved);

    TogglePrayerResponse result = trackingService.togglePrayer(TELEGRAM_ID, request);

    assertThat(result.date()).isEqualTo(today);
    assertThat(result.prayer()).isEqualTo("MAGHRIB");
  }

  @ParameterizedTest
  @EnumSource(StatsPeriod.class)
  void should_returnStats_when_anyPeriod(StatsPeriod period) {
    given(
            trackingRepository.findByTelegramIdAndDateBetween(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(List.of());

    PrayerStatsResponse result = trackingService.getStats(TELEGRAM_ID, period);

    assertThat(result.period()).isEqualTo(period.name());
    assertThat(result.total()).isPositive();
    assertThat(result.completed()).isZero();
    assertThat(result.percentage()).isZero();
    assertThat(result.streak()).isZero();
    assertThat(result.byPrayer()).hasSize(PrayerName.values().length);
  }

  @Test
  void should_calculateStreak_when_consecutiveDaysCompleted() {
    LocalDate today = LocalDate.now();
    List<PrayerTrackingEntity> entries = buildCompleteDayEntries(today, 3);

    given(
            trackingRepository.findByTelegramIdAndDateBetween(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(entries);

    PrayerStatsResponse result = trackingService.getStats(TELEGRAM_ID, StatsPeriod.WEEK);

    assertThat(result.streak()).isEqualTo(3);
  }

  @Test
  void should_breakStreak_when_dayMissed() {
    LocalDate today = LocalDate.now();
    // Complete today and yesterday, skip day before
    List<PrayerTrackingEntity> entries = new ArrayList<>();
    entries.addAll(buildCompleteDayEntries(today, 2));
    // Day -2 is missing, day -3 is complete
    entries.addAll(buildSingleDayEntries(today.minusDays(3)));

    given(
            trackingRepository.findByTelegramIdAndDateBetween(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(entries);

    PrayerStatsResponse result = trackingService.getStats(TELEGRAM_ID, StatsPeriod.WEEK);

    assertThat(result.streak()).isEqualTo(2);
  }

  @Test
  void should_calculatePercentage_when_someCompleted() {
    LocalDate today = LocalDate.now();
    List<PrayerTrackingEntity> entries = buildCompleteDayEntries(today, 1);

    given(
            trackingRepository.findByTelegramIdAndDateBetween(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(entries);

    PrayerStatsResponse result = trackingService.getStats(TELEGRAM_ID, StatsPeriod.WEEK);

    assertThat(result.completed()).isEqualTo(5);
    assertThat(result.percentage()).isPositive();
  }

  @Test
  void should_returnZeroStreak_when_noEntries() {
    given(
            trackingRepository.findByTelegramIdAndDateBetween(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(List.of());

    PrayerStatsResponse result = trackingService.getStats(TELEGRAM_ID, StatsPeriod.WEEK);

    assertThat(result.streak()).isZero();
  }

  @Test
  void should_countAllPrayerTypes_when_buildingStats() {
    given(
            trackingRepository.findByTelegramIdAndDateBetween(
                eq(TELEGRAM_ID), any(LocalDate.class), any(LocalDate.class)))
        .willReturn(List.of());

    PrayerStatsResponse result = trackingService.getStats(TELEGRAM_ID, StatsPeriod.WEEK);

    assertThat(result.byPrayer()).containsKeys("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA");
  }

  private List<PrayerTrackingEntity> buildCompleteDayEntries(LocalDate endDate, int days) {
    List<PrayerTrackingEntity> entries = new ArrayList<>();
    for (int d = 0; d < days; d++) {
      entries.addAll(buildSingleDayEntries(endDate.minusDays(d)));
    }
    return entries;
  }

  private List<PrayerTrackingEntity> buildSingleDayEntries(LocalDate date) {
    List<PrayerTrackingEntity> entries = new ArrayList<>();
    for (PrayerName prayer : PrayerName.values()) {
      entries.add(
          PrayerTrackingEntity.builder()
              .id(UUID.randomUUID())
              .telegramId(TELEGRAM_ID)
              .date(date)
              .prayerName(prayer)
              .prayed(true)
              .toggledAt(Instant.now())
              .build());
    }
    return entries;
  }
}
