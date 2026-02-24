package uz.myrafeeq.api.service.prayer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
  @InjectMocks private PrayerTrackingService prayerTrackingService;

  @Test
  void getTrackingByDate() {
    LocalDate date = LocalDate.of(2026, 2, 24);
    PrayerTrackingEntity entity =
        PrayerTrackingEntity.builder()
            .id(UUID.randomUUID())
            .telegramId(TELEGRAM_ID)
            .date(date)
            .prayerName(PrayerName.FAJR)
            .prayed(true)
            .toggledAt(Instant.now())
            .build();

    List<PrayerTrackingEntity> entities = List.of(entity);
    PrayerTrackingResponse expectedResponse =
        PrayerTrackingResponse.builder()
            .tracking(Map.of("2026-02-24", Map.of("FAJR", true)))
            .build();

    when(trackingRepository.findByTelegramIdAndDate(TELEGRAM_ID, date)).thenReturn(entities);
    when(trackingMapper.toTrackingResponse(entities)).thenReturn(expectedResponse);

    PrayerTrackingResponse result =
        prayerTrackingService.getTracking(TELEGRAM_ID, date, null, null);

    assertThat(result.tracking()).containsKey("2026-02-24");
    assertThat(result.tracking().get("2026-02-24")).containsEntry("FAJR", true);
    verify(trackingRepository).findByTelegramIdAndDate(TELEGRAM_ID, date);
  }

  @Test
  void getTrackingByRange() {
    LocalDate from = LocalDate.of(2026, 2, 20);
    LocalDate to = LocalDate.of(2026, 2, 24);

    List<PrayerTrackingEntity> entities = Collections.emptyList();
    PrayerTrackingResponse expectedResponse =
        PrayerTrackingResponse.builder().tracking(Map.of()).build();

    when(trackingRepository.findByTelegramIdAndDateBetween(TELEGRAM_ID, from, to))
        .thenReturn(entities);
    when(trackingMapper.toTrackingResponse(entities)).thenReturn(expectedResponse);

    PrayerTrackingResponse result = prayerTrackingService.getTracking(TELEGRAM_ID, null, from, to);

    assertThat(result.tracking()).isEmpty();
    verify(trackingRepository).findByTelegramIdAndDateBetween(TELEGRAM_ID, from, to);
  }

  @Test
  void getTrackingDefaultsToToday() {
    LocalDate today = LocalDate.now();

    List<PrayerTrackingEntity> entities = Collections.emptyList();
    PrayerTrackingResponse expectedResponse =
        PrayerTrackingResponse.builder().tracking(Map.of()).build();

    when(trackingRepository.findByTelegramIdAndDate(TELEGRAM_ID, today)).thenReturn(entities);
    when(trackingMapper.toTrackingResponse(entities)).thenReturn(expectedResponse);

    PrayerTrackingResponse result =
        prayerTrackingService.getTracking(TELEGRAM_ID, null, null, null);

    assertThat(result).isNotNull();
    verify(trackingRepository).findByTelegramIdAndDate(TELEGRAM_ID, today);
  }

  @Test
  void togglePrayerCreatesNew() {
    LocalDate today = LocalDate.now();
    TogglePrayerRequest request = new TogglePrayerRequest(today, PrayerName.FAJR, true);

    when(trackingRepository.findByTelegramIdAndDateAndPrayerName(
            TELEGRAM_ID, today, PrayerName.FAJR))
        .thenReturn(Optional.empty());

    PrayerTrackingEntity savedEntity =
        PrayerTrackingEntity.builder()
            .id(UUID.randomUUID())
            .telegramId(TELEGRAM_ID)
            .date(today)
            .prayerName(PrayerName.FAJR)
            .prayed(true)
            .toggledAt(Instant.now())
            .build();

    when(trackingRepository.save(any(PrayerTrackingEntity.class))).thenReturn(savedEntity);

    TogglePrayerResponse result = prayerTrackingService.togglePrayer(TELEGRAM_ID, request);

    assertThat(result.date()).isEqualTo(today.toString());
    assertThat(result.prayer()).isEqualTo("FAJR");
    assertThat(result.prayed()).isTrue();
    assertThat(result.toggledAt()).isNotNull();
    verify(trackingRepository).save(any(PrayerTrackingEntity.class));
  }

  @Test
  void togglePrayerUpdatesExisting() {
    LocalDate today = LocalDate.now();
    TogglePrayerRequest request = new TogglePrayerRequest(today, PrayerName.DHUHR, false);

    PrayerTrackingEntity existingEntity =
        PrayerTrackingEntity.builder()
            .id(UUID.randomUUID())
            .telegramId(TELEGRAM_ID)
            .date(today)
            .prayerName(PrayerName.DHUHR)
            .prayed(true)
            .toggledAt(Instant.now().minusSeconds(3600))
            .build();

    when(trackingRepository.findByTelegramIdAndDateAndPrayerName(
            TELEGRAM_ID, today, PrayerName.DHUHR))
        .thenReturn(Optional.of(existingEntity));

    PrayerTrackingEntity savedEntity =
        PrayerTrackingEntity.builder()
            .id(existingEntity.getId())
            .telegramId(TELEGRAM_ID)
            .date(today)
            .prayerName(PrayerName.DHUHR)
            .prayed(false)
            .toggledAt(Instant.now())
            .build();

    when(trackingRepository.save(any(PrayerTrackingEntity.class))).thenReturn(savedEntity);

    TogglePrayerResponse result = prayerTrackingService.togglePrayer(TELEGRAM_ID, request);

    assertThat(result.prayer()).isEqualTo("DHUHR");
    assertThat(result.prayed()).isFalse();
    verify(trackingRepository).save(any(PrayerTrackingEntity.class));
  }

  @Test
  void togglePrayerRejectsFutureDate() {
    LocalDate futureDate = LocalDate.now().plusDays(1);
    TogglePrayerRequest request = new TogglePrayerRequest(futureDate, PrayerName.FAJR, true);

    assertThatThrownBy(() -> prayerTrackingService.togglePrayer(TELEGRAM_ID, request))
        .isInstanceOf(TrackingValidationException.class)
        .hasMessageContaining("Cannot track prayers for future dates");
  }

  @Test
  void togglePrayerRejectsOldDate() {
    LocalDate oldDate = LocalDate.now().minusDays(8);
    TogglePrayerRequest request = new TogglePrayerRequest(oldDate, PrayerName.ASR, true);

    assertThatThrownBy(() -> prayerTrackingService.togglePrayer(TELEGRAM_ID, request))
        .isInstanceOf(TrackingValidationException.class)
        .hasMessageContaining("Cannot track prayers older than 7 days");
  }

  @Test
  void getStatsCalculatesPercentage() {
    LocalDate today = LocalDate.now();
    LocalDate from = today.minusDays(StatsPeriod.WEEK.getDays());

    List<PrayerTrackingEntity> entities = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      entities.add(
          PrayerTrackingEntity.builder()
              .id(UUID.randomUUID())
              .telegramId(TELEGRAM_ID)
              .date(today.minusDays(i))
              .prayerName(PrayerName.FAJR)
              .prayed(true)
              .toggledAt(Instant.now())
              .build());
    }

    when(trackingRepository.findByTelegramIdAndDateBetween(eq(TELEGRAM_ID), any(), any()))
        .thenReturn(entities);

    PrayerStatsResponse result = prayerTrackingService.getStats(TELEGRAM_ID, StatsPeriod.WEEK);

    assertThat(result.period()).isEqualTo("WEEK");
    assertThat(result.total()).isGreaterThan(0);
    assertThat(result.completed()).isEqualTo(3);
    assertThat(result.percentage()).isGreaterThan(0);
    assertThat(result.percentage()).isLessThanOrEqualTo(100);
    assertThat(result.byPrayer()).containsKey("FAJR");
    assertThat(result.byPrayer().get("FAJR").completed()).isEqualTo(3);
  }

  @Test
  void getStatsCalculatesStreak() {
    LocalDate today = LocalDate.now();

    List<PrayerTrackingEntity> entities = new ArrayList<>();
    for (int dayOffset = 0; dayOffset < 3; dayOffset++) {
      for (PrayerName prayer : PrayerName.values()) {
        entities.add(
            PrayerTrackingEntity.builder()
                .id(UUID.randomUUID())
                .telegramId(TELEGRAM_ID)
                .date(today.minusDays(dayOffset))
                .prayerName(prayer)
                .prayed(true)
                .toggledAt(Instant.now())
                .build());
      }
    }

    when(trackingRepository.findByTelegramIdAndDateBetween(eq(TELEGRAM_ID), any(), any()))
        .thenReturn(entities);

    PrayerStatsResponse result = prayerTrackingService.getStats(TELEGRAM_ID, StatsPeriod.WEEK);

    assertThat(result.streak()).isGreaterThanOrEqualTo(3);
  }
}
