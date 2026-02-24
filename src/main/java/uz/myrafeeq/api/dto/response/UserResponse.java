package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "User information")
public record UserResponse(
    @Schema(description = "Telegram user ID", example = "123456789") Long telegramId,
    @Schema(description = "User's first name", example = "Doston") String firstName,
    @Schema(description = "User's language code", example = "uz") String languageCode,
    @Schema(description = "Whether onboarding is completed", example = "false")
        Boolean onboardingCompleted) {}
