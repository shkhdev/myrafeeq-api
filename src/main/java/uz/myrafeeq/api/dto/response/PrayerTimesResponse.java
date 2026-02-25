package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Map;
import lombok.Builder;

@Builder
@Schema(description = "Prayer times for a specific date")
public record PrayerTimesResponse(
    @Schema(description = "Gregorian date", example = "2026-02-24") LocalDate date,
    @Schema(description = "Hijri date", example = "6 Sha'ban 1447") String hijriDate,
    @Schema(description = "City name", example = "Tashkent") String city,
    @Schema(description = "Prayer times") PrayerTimesDto times,
    @Schema(description = "Calculation metadata") PrayerTimesMeta meta) {

  @Builder
  @Schema(description = "Individual prayer times")
  public record PrayerTimesDto(
      @Schema(description = "Fajr time", example = "05:42") String fajr,
      @Schema(description = "Sunrise time", example = "07:05") String sunrise,
      @Schema(description = "Dhuhr time", example = "12:35") String dhuhr,
      @Schema(description = "Asr time", example = "15:48") String asr,
      @Schema(description = "Maghrib time", example = "18:02") String maghrib,
      @Schema(description = "Isha time", example = "19:25") String isha) {}

  @Builder
  @Schema(description = "Prayer calculation metadata")
  public record PrayerTimesMeta(
      @Schema(description = "Calculation method used", example = "MWL") String calculationMethod,
      @Schema(description = "Juristic method for Asr", example = "HANAFI") String madhab,
      @Schema(description = "Manual adjustments applied") Map<String, Integer> adjustments) {}
}
