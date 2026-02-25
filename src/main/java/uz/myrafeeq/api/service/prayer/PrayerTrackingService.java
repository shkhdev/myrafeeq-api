package uz.myrafeeq.api.service.prayer;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class PrayerTrackingService {

  private static final int MAX_PAST_DAYS = 7;
  private static final int MAX_STREAK_LOOKBACK = 365;

  private final PrayerTrackingRepository trackingRepository;
  private final PrayerTrackingMapper trackingMapper;

  @Transactional(readOnly = true)
  public PrayerTrackingResponse getTracking(
      Long telegramId, LocalDate date, LocalDate from, LocalDate to) {
    List<PrayerTrackingEntity> entities;

    if (date != null) {
      entities = trackingRepository.findByTelegramIdAndDate(telegramId, date);
    } else if (from != null && to != null) {
      entities = trackingRepository.findByTelegramIdAndDateBetween(telegramId, from, to);
    } else {
      entities = trackingRepository.findByTelegramIdAndDate(telegramId, LocalDate.now());
    }

    return trackingMapper.toTrackingResponse(entities);
  }

  @Transactional
  public TogglePrayerResponse togglePrayer(Long telegramId, TogglePrayerRequest request) {
    LocalDate today = LocalDate.now();

    if (request.date().isAfter(today)) {
      throw new TrackingValidationException("Cannot track prayers for future dates");
    }

    if (request.date().isBefore(today.minusDays(MAX_PAST_DAYS))) {
      throw new TrackingValidationException(
          "Cannot track prayers older than " + MAX_PAST_DAYS + " days");
    }

    Instant now = Instant.now();

    PrayerTrackingEntity entity =
        trackingRepository
            .findByTelegramIdAndDateAndPrayerName(telegramId, request.date(), request.prayer())
            .map(
                existing -> {
                  existing.setPrayed(request.prayed());
                  existing.setToggledAt(now);
                  return existing;
                })
            .orElseGet(
                () ->
                    PrayerTrackingEntity.builder()
                        .telegramId(telegramId)
                        .date(request.date())
                        .prayerName(request.prayer())
                        .prayed(request.prayed())
                        .toggledAt(now)
                        .build());

    entity = trackingRepository.save(entity);

    return TogglePrayerResponse.builder()
        .date(entity.getDate())
        .prayer(entity.getPrayerName().name())
        .prayed(entity.getPrayed())
        .toggledAt(entity.getToggledAt())
        .build();
  }

  @Transactional(readOnly = true)
  public PrayerStatsResponse getStats(Long telegramId, StatsPeriod period) {
    LocalDate today = LocalDate.now();
    LocalDate lookbackStart = today.minusDays(MAX_STREAK_LOOKBACK);

    List<PrayerTrackingEntity> allEntries =
        trackingRepository.findByTelegramIdAndDateBetween(telegramId, lookbackStart, today);

    LocalDate statsFrom = today.minusDays(period.getDays());
    List<PrayerTrackingEntity> statsEntries =
        allEntries.stream().filter(e -> !e.getDate().isBefore(statsFrom)).toList();

    long totalDays = statsFrom.until(today).getDays() + 1;
    int totalPrayers = (int) (totalDays * PrayerName.values().length);

    int completedPrayers = 0;
    Map<String, PrayerStatsResponse.PrayerStatDetail> byPrayer = new LinkedHashMap<>();

    for (PrayerName prayer : PrayerName.values()) {
      int prayerTotal = (int) totalDays;
      int prayerCompleted =
          (int)
              statsEntries.stream()
                  .filter(e -> e.getPrayerName() == prayer && Boolean.TRUE.equals(e.getPrayed()))
                  .count();
      completedPrayers += prayerCompleted;
      byPrayer.put(
          prayer.name(),
          PrayerStatsResponse.PrayerStatDetail.builder()
              .total(prayerTotal)
              .completed(prayerCompleted)
              .build());
    }

    int percentage = totalPrayers > 0 ? (completedPrayers * 100) / totalPrayers : 0;

    int streak = calculateStreak(allEntries, today);

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

  private int calculateStreak(List<PrayerTrackingEntity> allEntries, LocalDate today) {
    Map<LocalDate, Long> completedByDate =
        allEntries.stream()
            .filter(e -> Boolean.TRUE.equals(e.getPrayed()))
            .collect(Collectors.groupingBy(PrayerTrackingEntity::getDate, Collectors.counting()));

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
