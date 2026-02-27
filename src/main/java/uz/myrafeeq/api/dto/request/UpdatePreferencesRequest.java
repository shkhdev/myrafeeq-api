package uz.myrafeeq.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uz.myrafeeq.api.enums.CalculationMethod;
import uz.myrafeeq.api.enums.HighLatitudeRule;
import uz.myrafeeq.api.enums.Madhab;
import uz.myrafeeq.api.enums.ReminderTiming;
import uz.myrafeeq.api.enums.ThemePreference;
import uz.myrafeeq.api.enums.TimeFormat;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update user preferences (all fields optional)")
public class UpdatePreferencesRequest {

  @Size(max = 10) @Schema(description = "Language code", example = "en")
  private String languageCode;

  @Size(min = 1, max = 50) @Schema(description = "City ID", example = "tashkent")
  private String cityId;

  @Schema(description = "Prayer calculation method", example = "MWL")
  private CalculationMethod calculationMethod;

  @Schema(description = "Juristic method for Asr", example = "HANAFI")
  private Madhab madhab;

  @Schema(description = "High latitude adjustment rule", example = "MIDDLE_OF_NIGHT")
  private HighLatitudeRule highLatitudeRule;

  @Min(-2) @Max(2) @Schema(description = "Hijri date correction (-2 to +2)", example = "0")
  private Integer hijriCorrection;

  @Schema(description = "Time display format", example = "24h")
  private TimeFormat timeFormat;

  @Schema(description = "Theme preference", example = "SYSTEM")
  private ThemePreference theme;

  @Schema(description = "Whether notifications are enabled", example = "true")
  private Boolean notificationsEnabled;

  @Schema(description = "Reminder timing preference", example = "FIVE_MIN")
  private ReminderTiming reminderTiming;

  @Size(max = 10) @Schema(description = "Per-prayer notification settings")
  private Map<String, Boolean> prayerNotifications;

  @Size(max = 10) @Schema(description = "Per-prayer manual time adjustments in minutes")
  private Map<String, Integer> manualAdjustments;
}
