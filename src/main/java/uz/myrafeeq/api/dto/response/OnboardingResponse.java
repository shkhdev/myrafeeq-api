package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Onboarding completion response")
public record OnboardingResponse(
    @Schema(description = "User information") UserResponse user,
    @Schema(description = "User preferences") UserPreferencesResponse preferences) {}
