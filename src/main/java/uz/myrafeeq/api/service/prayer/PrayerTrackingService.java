package uz.myrafeeq.api.service.prayer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import uz.myrafeeq.api.repository.projection.DateCountProjection;
import uz.myrafeeq.api.repository.projection.PrayerCountProjection;
import uz.myrafeeq.api.service.user.UserTimezoneResolver;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrayerTrackingService {

  private static final int MAX_PAST_DAYS = 7;
  private static final int MAX_DATE_RANGE_DAYS = 90;
  private static final int MAX_STREAK_LOOKBACK = 365;

  private final PrayerTrackingRepository trackingRepository;
  private final PrayerTrackingMapper trackingMapper;
  private final UserTimezoneResolver userTimezoneResolver;

  @Transactional(readOnly = true)
  public PrayerTrackingResponse getTracking(
      Long telegramId, LocalDate date, LocalDate from, LocalDate to) {
    List<PrayerTrackingEntity> entities;

    if (date != null) {
      entities = trackingRepository.findByTelegramIdAndPrayerDate(telegramId, date);
    } else if (from != null && to != null) {
      if (from.isAfter(to)) {
        throw new TrackingValidationException("'from' date must not be after 'to' date");
      }
      if (ChronoUnit.DAYS.between(from, to) > MAX_DATE_RANGE_DAYS) {
        throw new TrackingValidationException(
            "Date range cannot exceed " + MAX_DATE_RANGE_DAYS + " days");
      }
      entities = trackingRepository.findByTelegramIdAndPrayerDateBetween(telegramId, from, to);
    } else {
      entities =
          trackingRepository.findByTelegramIdAndPrayerDate(
              telegramId, LocalDate.now(userTimezoneResolver.resolveTimezone(telegramId)));
    }

    return trackingMapper.toTrackingResponse(entities);
  }

  @Transactional
  @Caching(
      evict = {
        @CacheEvict(value = "prayerStats", key = "#telegramId + '-WEEK'"),
        @CacheEvict(value = "prayerStats", key = "#telegramId + '-MONTH'"),
        @CacheEvict(value = "prayerStats", key = "#telegramId + '-YEAR'")
      })
  public TogglePrayerResponse togglePrayer(Long telegramId, TogglePrayerRequest request) {
    LocalDate today = LocalDate.now(userTimezoneResolver.resolveTimezone(telegramId));

    if (request.getDate().isAfter(today)) {
      throw new TrackingValidationException("Cannot track prayers for future dates");
    }

    if (request.getDate().isBefore(today.minusDays(MAX_PAST_DAYS))) {
      throw new TrackingValidationException(
          "Cannot track prayers older than " + MAX_PAST_DAYS + " days");
    }

    Instant now = Instant.now();

    PrayerTrackingEntity entity =
        trackingRepository
            .findByTelegramIdAndPrayerDateAndPrayerName(
                telegramId, request.getDate(), request.getPrayer())
            .map(
                existing -> {
                  existing.setPrayed(request.getPrayed());
                  existing.setToggledAt(now);
                  return existing;
                })
            .orElseGet(
                () ->
                    PrayerTrackingEntity.builder()
                        .telegramId(telegramId)
                        .prayerDate(request.getDate())
                        .prayerName(request.getPrayer())
                        .prayed(request.getPrayed())
                        .toggledAt(now)
                        .build());

    entity = trackingRepository.save(entity);

    log.info(
        "Prayer toggled: user={}, date={}, prayer={}, prayed={}",
        telegramId,
        request.getDate(),
        request.getPrayer(),
        request.getPrayed());

    return TogglePrayerResponse.builder()
        .date(entity.getPrayerDate())
        .prayer(entity.getPrayerName().name())
        .prayed(entity.getPrayed())
        .toggledAt(entity.getToggledAt())
        .build();
  }

  @Transactional(readOnly = true)
  @Cacheable(value = "prayerStats", key = "#telegramId + '-' + #period.name()")
  public PrayerStatsResponse getStats(Long telegramId, StatsPeriod period) {
    LocalDate today = LocalDate.now(userTimezoneResolver.resolveTimezone(telegramId));
    LocalDate statsFrom = today.minusDays(period.getDays());
    LocalDate streakFrom = today.minusDays(MAX_STREAK_LOOKBACK);

    List<PrayerCountProjection> prayerCounts =
        trackingRepository.countCompletedByPrayer(telegramId, statsFrom, today);
    Map<PrayerName, Long> completedMap =
        prayerCounts.stream()
            .collect(
                Collectors.toMap(
                    PrayerCountProjection::getPrayerName, PrayerCountProjection::getCount));

    long totalDays = statsFrom.until(today).getDays() + 1;
    int totalPrayers = (int) (totalDays * PrayerName.values().length);

    int completedPrayers = 0;
    Map<String, PrayerStatsResponse.PrayerStatDetail> byPrayer = new LinkedHashMap<>();

    for (PrayerName prayer : PrayerName.values()) {
      int prayerTotal = (int) totalDays;
      int prayerCompleted = completedMap.getOrDefault(prayer, 0L).intValue();
      completedPrayers += prayerCompleted;
      byPrayer.put(
          prayer.name(),
          PrayerStatsResponse.PrayerStatDetail.builder()
              .total(prayerTotal)
              .completed(prayerCompleted)
              .build());
    }

    int percentage = totalPrayers > 0 ? (completedPrayers * 100) / totalPrayers : 0;

    List<DateCountProjection> dailyCounts =
        trackingRepository.countCompletedByDate(telegramId, streakFrom, today);
    int streak = calculateStreak(dailyCounts, today);

    return PrayerStatsResponse.builder()
        .period(period.name())
        .from(statsFrom)
        .to(today)
        .total(totalPrayers)
        .completed(completedPrayers)
        .percentage(percentage)
        .byPrayer(byPrayer)
        .streak(streak)
        .build();
  }

  private int calculateStreak(List<DateCountProjection> dailyCounts, LocalDate today) {
    Map<LocalDate, Long> completedByDate =
        dailyCounts.stream()
            .collect(
                Collectors.toMap(
                    DateCountProjection::getPrayerDate, DateCountProjection::getCount));

    int expectedPrayers = PrayerName.values().length;
    int streak = 0;

    for (int i = 0; i < MAX_STREAK_LOOKBACK; i++) {
      LocalDate date = today.minusDays(i);
      long completed = completedByDate.getOrDefault(date, 0L);

      if (completed == expectedPrayers) {
        streak++;
      } else {
        break;
      }
    }

    return streak;
  }
}
