package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Prayer completion statistics")
public class PrayerStatsResponse {

  @Schema(description = "Statistics period", example = "week")
  private final String period;

  @Schema(description = "Period start date", example = "2026-02-17")
  private final LocalDate from;

  @Schema(description = "Period end date", example = "2026-02-24")
  private final LocalDate to;

  @Schema(description = "Total prayer slots", example = "35")
  private final Integer total;

  @Schema(description = "Completed prayers", example = "28")
  private final Integer completed;

  @Schema(description = "Completion percentage", example = "80")
  private final Integer percentage;

  @Schema(description = "Breakdown by prayer name")
  private final Map<String, PrayerStatDetail> byPrayer;

  @Schema(description = "Current consecutive days streak", example = "5")
  private final Integer streak;

  @Getter
  @Builder
  @Schema(description = "Statistics for a single prayer")
  public static class PrayerStatDetail {

    @Schema(description = "Total occurrences", example = "7")
    private final Integer total;

    @Schema(description = "Completed count", example = "5")
    private final Integer completed;
  }
}
