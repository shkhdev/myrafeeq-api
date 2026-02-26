package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Onboarding completion response")
public class OnboardingResponse {

  @Schema(description = "User information")
  private final UserResponse user;

  @Schema(description = "User preferences")
  private final UserPreferencesResponse preferences;
}
