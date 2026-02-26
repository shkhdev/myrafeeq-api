package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Aggregated dashboard data for the main screen")
public class DashboardResponse {

  @Schema(description = "Today's prayer times")
  private final PrayerTimesResponse prayerTimes;

  @Schema(description = "Today's prayer tracking")
  private final PrayerTrackingResponse tracking;

  @Schema(description = "Weekly prayer statistics")
  private final PrayerStatsResponse stats;
}
