package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "User information")
public class UserResponse {

  @Schema(description = "Telegram user ID", example = "123456789")
  private final Long telegramId;

  @Schema(description = "User's first name", example = "Doston")
  private final String firstName;

  @Schema(description = "User's language code", example = "uz")
  private final String languageCode;

  @Schema(description = "Whether onboarding is completed", example = "false")
  private final Boolean onboardingCompleted;
}
