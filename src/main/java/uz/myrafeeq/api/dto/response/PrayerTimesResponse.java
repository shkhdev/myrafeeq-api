package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Prayer times for a specific date")
public class PrayerTimesResponse {

  @Schema(description = "Gregorian date", example = "2026-02-24")
  private final LocalDate date;

  @Schema(description = "Hijri date", example = "6 Sha'ban 1447")
  private final String hijriDate;

  @Schema(description = "City name", example = "Tashkent")
  private final String city;

  @Schema(description = "Prayer times")
  private final PrayerTimesDto times;

  @Schema(description = "Calculation metadata")
  private final PrayerTimesMeta meta;

  @Getter
  @Builder
  @Schema(description = "Individual prayer times")
  public static class PrayerTimesDto {

    @Schema(description = "Fajr time", example = "05:42")
    private final String fajr;

    @Schema(description = "Sunrise time", example = "07:05")
    private final String sunrise;

    @Schema(description = "Dhuhr time", example = "12:35")
    private final String dhuhr;

    @Schema(description = "Asr time", example = "15:48")
    private final String asr;

    @Schema(description = "Maghrib time", example = "18:02")
    private final String maghrib;

    @Schema(description = "Isha time", example = "19:25")
    private final String isha;
  }

  @Getter
  @Builder
  @Schema(description = "Prayer calculation metadata")
  public static class PrayerTimesMeta {

    @Schema(description = "Calculation method used", example = "MWL")
    private final String calculationMethod;

    @Schema(description = "Juristic method for Asr", example = "HANAFI")
    private final String madhab;

    @Schema(description = "Manual adjustments applied")
    private final Map<String, Integer> adjustments;
  }
}
