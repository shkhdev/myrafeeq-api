package uz.myrafeeq.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uz.myrafeeq.api.enums.ReminderTiming;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to complete user onboarding")
public class OnboardingRequest {

  @NotBlank @Size(max = 50) @Schema(description = "Selected city ID", example = "tashkent")
  private String cityId;

  @DecimalMin("-90") @DecimalMax("90") @Schema(description = "User latitude", example = "41.2995")
  private Double latitude;

  @DecimalMin("-180") @DecimalMax("180") @Schema(description = "User longitude", example = "69.2401")
  private Double longitude;

  @NotNull @Schema(description = "Whether notifications are enabled", example = "true")
  private Boolean notificationsEnabled;

  @Size(max = 10) @Schema(description = "Per-prayer notification settings")
  private Map<String, Boolean> prayerNotifications;

  @Schema(description = "Reminder timing preference", example = "ON_TIME")
  private ReminderTiming reminderTiming;
}
