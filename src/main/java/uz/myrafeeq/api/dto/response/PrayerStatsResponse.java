package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Builder;

@Builder
@Schema(description = "Prayer completion statistics")
public record PrayerStatsResponse(
    @Schema(description = "Statistics period", example = "week") String period,
    @Schema(description = "Period start date", example = "2026-02-17") String from,
    @Schema(description = "Period end date", example = "2026-02-24") String to,
    @Schema(description = "Total prayer slots", example = "35") Integer total,
    @Schema(description = "Completed prayers", example = "28") Integer completed,
    @Schema(description = "Completion percentage", example = "80") Integer percentage,
    @Schema(description = "Breakdown by prayer name") Map<String, PrayerStatDetail> byPrayer,
    @Schema(description = "Current consecutive days streak", example = "5") Integer streak) {

  @Builder
  @Schema(description = "Statistics for a single prayer")
  public record PrayerStatDetail(
      @Schema(description = "Total occurrences", example = "7") Integer total,
      @Schema(description = "Completed count", example = "5") Integer completed) {}
}
