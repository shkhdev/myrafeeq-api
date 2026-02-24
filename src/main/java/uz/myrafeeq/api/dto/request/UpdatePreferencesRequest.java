package uz.myrafeeq.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import uz.myrafeeq.api.enums.CalculationMethod;
import uz.myrafeeq.api.enums.HighLatitudeRule;
import uz.myrafeeq.api.enums.Madhab;
import uz.myrafeeq.api.enums.ReminderTiming;
import uz.myrafeeq.api.enums.ThemePreference;
import uz.myrafeeq.api.enums.TimeFormat;

@Schema(description = "Request to update user preferences (all fields optional)")
public record UpdatePreferencesRequest(
    @Schema(description = "City ID", example = "tashkent") String cityId,
    @Schema(description = "Prayer calculation method", example = "MWL")
        CalculationMethod calculationMethod,
    @Schema(description = "Juristic method for Asr", example = "HANAFI") Madhab madhab,
    @Schema(description = "High latitude adjustment rule", example = "MIDDLE_OF_NIGHT")
        HighLatitudeRule highLatitudeRule,
    @Schema(description = "Hijri date correction (-2 to +2)", example = "0")
        Integer hijriCorrection,
    @Schema(description = "Time display format", example = "24h") TimeFormat timeFormat,
    @Schema(description = "Theme preference", example = "SYSTEM") ThemePreference theme,
    @Schema(description = "Whether notifications are enabled", example = "true")
        Boolean notificationsEnabled,
    @Schema(description = "Reminder timing preference", example = "FIVE_MIN")
        ReminderTiming reminderTiming,
    @Schema(description = "Per-prayer notification settings")
        Map<String, Boolean> prayerNotifications,
    @Schema(description = "Per-prayer manual time adjustments in minutes")
        Map<String, Integer> manualAdjustments) {}
