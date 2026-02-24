package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Builder;

@Builder
@Schema(description = "User preferences")
public record UserPreferencesResponse(
    @Schema(description = "Selected city") CityResponse city,
    @Schema(description = "Prayer calculation method", example = "MWL") String calculationMethod,
    @Schema(description = "Juristic method for Asr", example = "HANAFI") String madhab,
    @Schema(description = "High latitude adjustment rule", example = "MIDDLE_OF_NIGHT")
        String highLatitudeRule,
    @Schema(description = "Hijri date correction", example = "0") Integer hijriCorrection,
    @Schema(description = "Time display format", example = "24h") String timeFormat,
    @Schema(description = "Theme preference", example = "SYSTEM") String theme,
    @Schema(description = "Whether notifications are enabled", example = "true")
        Boolean notificationsEnabled,
    @Schema(description = "Reminder timing preference", example = "ON_TIME") String reminderTiming,
    @Schema(description = "Per-prayer notification settings")
        Map<String, Boolean> prayerNotifications,
    @Schema(description = "Per-prayer manual time adjustments in minutes")
        Map<String, Integer> manualAdjustments) {}
