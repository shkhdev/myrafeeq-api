package uz.myrafeeq.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import uz.myrafeeq.api.enums.ReminderTiming;

@Schema(description = "Request to complete user onboarding")
public record OnboardingRequest(
    @NotBlank @Schema(description = "Selected city ID", example = "tashkent") String cityId,
    @Schema(description = "User latitude", example = "41.2995") Double latitude,
    @Schema(description = "User longitude", example = "69.2401") Double longitude,
    @NotNull @Schema(description = "Whether notifications are enabled", example = "true")
        Boolean notificationsEnabled,
    @Schema(description = "Per-prayer notification settings")
        Map<String, Boolean> prayerNotifications,
    @Schema(description = "Reminder timing preference", example = "ON_TIME")
        ReminderTiming reminderTiming) {}
