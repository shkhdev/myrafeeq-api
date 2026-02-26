package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "User preferences")
public class UserPreferencesResponse {

  @Schema(description = "Selected city")
  private final CityResponse city;

  @Schema(description = "Prayer calculation method", example = "MWL")
  private final String calculationMethod;

  @Schema(description = "Juristic method for Asr", example = "HANAFI")
  private final String madhab;

  @Schema(description = "High latitude adjustment rule", example = "MIDDLE_OF_NIGHT")
  private final String highLatitudeRule;

  @Schema(description = "Hijri date correction", example = "0")
  private final Integer hijriCorrection;

  @Schema(description = "Time display format", example = "24h")
  private final String timeFormat;

  @Schema(description = "Theme preference", example = "SYSTEM")
  private final String theme;

  @Schema(description = "Whether notifications are enabled", example = "true")
  private final Boolean notificationsEnabled;

  @Schema(description = "Reminder timing preference", example = "ON_TIME")
  private final String reminderTiming;

  @Schema(description = "Per-prayer notification settings")
  private final Map<String, Boolean> prayerNotifications;

  @Schema(description = "Per-prayer manual time adjustments in minutes")
  private final Map<String, Integer> manualAdjustments;
}
