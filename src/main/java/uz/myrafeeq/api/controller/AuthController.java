package uz.myrafeeq.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Telegram Mini App authentication")
public class AuthController {

  private final TelegramAuthService telegramAuthService;

  @PostMapping("/validate")
  @Operation(
      summary = "Validate Telegram init data",
      description =
          """
          Validates Telegram Mini App init data, verifies HMAC signature, \
          upserts user, and returns a JWT token.""")
  @ApiResponse(responseCode = "201", description = "Authentication successful")
  @ApiResponse(
      responseCode = "401",
      description = "Invalid init data",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<AuthResponse> validate(@Valid @RequestBody TelegramAuthRequest request) {

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(telegramAuthService.authenticate(request));
  }
}
