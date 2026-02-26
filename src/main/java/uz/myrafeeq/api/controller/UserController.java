package uz.myrafeeq.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.myrafeeq.api.dto.request.OnboardingRequest;
import uz.myrafeeq.api.dto.request.UpdatePreferencesRequest;
import uz.myrafeeq.api.dto.response.ErrorResponse;
import uz.myrafeeq.api.dto.response.OnboardingResponse;
import uz.myrafeeq.api.dto.response.UserPreferencesResponse;
import uz.myrafeeq.api.service.user.OnboardingService;
import uz.myrafeeq.api.service.user.UserPreferencesService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "User", description = "User preferences and onboarding")
public class UserController {

  private final UserPreferencesService preferencesService;
  private final OnboardingService onboardingService;

  @GetMapping("/preferences")
  @Operation(
      summary = "Get user preferences",
      description = "Returns the current user's preferences.")
  @ApiResponse(responseCode = "200", description = "Preferences retrieved successfully")
  @ApiResponse(
      responseCode = "404",
      description = "Preferences not found",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<UserPreferencesResponse> getPreferences(
      @AuthenticationPrincipal Long telegramId) {
    return ResponseEntity.ok(preferencesService.getPreferences(telegramId));
  }

  @PatchMapping("/preferences")
  @Operation(
      summary = "Update user preferences",
      description =
          "Partially updates the current user's preferences. Only non-null fields are applied.")
  @ApiResponse(responseCode = "200", description = "Preferences updated successfully")
  @ApiResponse(
      responseCode = "404",
      description = "Preferences or city not found",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Validation error",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "409",
      description = "Concurrent modification conflict",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<UserPreferencesResponse> updatePreferences(
      @AuthenticationPrincipal Long telegramId,
      @Valid @RequestBody UpdatePreferencesRequest request) {

    return ResponseEntity.ok(preferencesService.updatePreferences(telegramId, request));
  }

  @PostMapping("/onboarding")
  @Operation(
      summary = "Complete onboarding",
      description =
          """
          Completes the user onboarding flow. Creates initial preferences \
          and marks onboarding as completed.""")
  @ApiResponse(responseCode = "201", description = "Onboarding completed successfully")
  @ApiResponse(
      responseCode = "409",
      description = "Onboarding already completed",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "404",
      description = "User or city not found",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Validation error",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<OnboardingResponse> completeOnboarding(
      @AuthenticationPrincipal Long telegramId, @Valid @RequestBody OnboardingRequest request) {

    return ResponseEntity.created(URI.create("/api/v1/user/preferences"))
        .body(onboardingService.completeOnboarding(telegramId, request));
  }
}
