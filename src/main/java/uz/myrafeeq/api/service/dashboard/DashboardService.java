package uz.myrafeeq.api.service.dashboard;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.myrafeeq.api.dto.response.DashboardResponse;
import uz.myrafeeq.api.dto.response.PrayerStatsResponse;
import uz.myrafeeq.api.dto.response.PrayerTimesResponse;
import uz.myrafeeq.api.dto.response.PrayerTrackingResponse;
import uz.myrafeeq.api.enums.StatsPeriod;
import uz.myrafeeq.api.service.prayer.PrayerTimesService;
import uz.myrafeeq.api.service.prayer.PrayerTrackingService;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final PrayerTimesService prayerTimesService;
  private final PrayerTrackingService trackingService;

  private static <T> T unwrap(CompletableFuture<T> future) {
    try {
      return future.join();
    } catch (CompletionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException re) {
        throw re;
      }
      if (cause instanceof Error err) {
        throw err;
      }
      throw new RuntimeException(cause);
    }
  }

  public DashboardResponse getDashboard(Long telegramId) {
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      CompletableFuture<List<PrayerTimesResponse>> timesFuture =
          CompletableFuture.supplyAsync(
              () -> prayerTimesService.calculatePrayerTimes(telegramId, null, 1), executor);

      CompletableFuture<PrayerTrackingResponse> trackingFuture =
          CompletableFuture.supplyAsync(
              () -> trackingService.getTracking(telegramId, null, null, null), executor);

      CompletableFuture<PrayerStatsResponse> statsFuture =
          CompletableFuture.supplyAsync(
              () -> trackingService.getStats(telegramId, StatsPeriod.WEEK), executor);

      CompletableFuture.allOf(timesFuture, trackingFuture, statsFuture).join();

      List<PrayerTimesResponse> times = unwrap(timesFuture);

      return DashboardResponse.builder()
          .prayerTimes(times.isEmpty() ? null : times.getFirst())
          .tracking(unwrap(trackingFuture))
          .stats(unwrap(statsFuture))
          .build();
    }
  }
}
