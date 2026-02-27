package uz.myrafeeq.api.service.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.myrafeeq.api.dto.response.DashboardResponse;
import uz.myrafeeq.api.dto.response.PrayerStatsResponse;
import uz.myrafeeq.api.dto.response.PrayerTimesResponse;
import uz.myrafeeq.api.dto.response.PrayerTrackingResponse;
import uz.myrafeeq.api.enums.StatsPeriod;
import uz.myrafeeq.api.exception.PreferencesNotFoundException;
import uz.myrafeeq.api.service.prayer.PrayerTimesService;
import uz.myrafeeq.api.service.prayer.PrayerTrackingService;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

  private static final Long TELEGRAM_ID = 123456789L;

  @Mock private PrayerTimesService prayerTimesService;
  @Mock private PrayerTrackingService trackingService;
  @InjectMocks private DashboardService dashboardService;

  @Test
  void should_returnDashboard_when_allServicesSucceed() {
    PrayerTimesResponse timesResponse = PrayerTimesResponse.builder().build();
    PrayerTrackingResponse trackingResponse =
        PrayerTrackingResponse.builder().tracking(Map.of()).build();
    PrayerStatsResponse statsResponse = PrayerStatsResponse.builder().period("WEEK").build();

    given(prayerTimesService.calculatePrayerTimes(eq(TELEGRAM_ID), any(), eq(1)))
        .willReturn(List.of(timesResponse));
    given(trackingService.getTracking(eq(TELEGRAM_ID), any(), any(), any()))
        .willReturn(trackingResponse);
    given(trackingService.getStats(TELEGRAM_ID, StatsPeriod.WEEK)).willReturn(statsResponse);

    DashboardResponse result = dashboardService.getDashboard(TELEGRAM_ID);

    assertThat(result.getPrayerTimes()).isNotNull();
    assertThat(result.getTracking()).isNotNull();
    assertThat(result.getStats()).isNotNull();
  }

  @Test
  void should_returnNullPrayerTimes_when_emptyTimesResult() {
    PrayerTrackingResponse trackingResponse =
        PrayerTrackingResponse.builder().tracking(Map.of()).build();
    PrayerStatsResponse statsResponse = PrayerStatsResponse.builder().period("WEEK").build();

    given(prayerTimesService.calculatePrayerTimes(eq(TELEGRAM_ID), any(), eq(1)))
        .willReturn(List.of());
    given(trackingService.getTracking(eq(TELEGRAM_ID), any(), any(), any()))
        .willReturn(trackingResponse);
    given(trackingService.getStats(TELEGRAM_ID, StatsPeriod.WEEK)).willReturn(statsResponse);

    DashboardResponse result = dashboardService.getDashboard(TELEGRAM_ID);

    assertThat(result.getPrayerTimes()).isNull();
  }

  @Test
  void should_propagateException_when_serviceThrows() {
    given(prayerTimesService.calculatePrayerTimes(eq(TELEGRAM_ID), any(), eq(1)))
        .willThrow(new PreferencesNotFoundException("No preferences for user " + TELEGRAM_ID));

    assertThatThrownBy(() -> dashboardService.getDashboard(TELEGRAM_ID))
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(PreferencesNotFoundException.class);
  }
}
