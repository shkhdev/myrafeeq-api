package uz.myrafeeq.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.myrafeeq.api.dto.request.TelegramAuthRequest;
import uz.myrafeeq.api.dto.response.AuthResponse;
import uz.myrafeeq.api.dto.response.ErrorResponse;
import uz.myrafeeq.api.service.auth.TelegramAuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Telegram Mini App authentication")
public class AuthController {

  private final TelegramAuthService telegramAuthService;

  @PostMapping("/token")
  @Operation(
      summary = "Authenticate and obtain token",
      description =
          """
          Validates Telegram Mini App init data, verifies HMAC signature,
          upserts user, and returns a JWT token.""")
  @ApiResponse(responseCode = "200", description = "Authentication successful")
  @ApiResponse(
      responseCode = "401",
      description = "Invalid init data",
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
  public ResponseEntity<AuthResponse> authenticate(
      @Valid @RequestBody TelegramAuthRequest request) {

    return ResponseEntity.ok(telegramAuthService.authenticate(request));
  }
}
