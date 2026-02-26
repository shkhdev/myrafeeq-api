package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Authentication response with JWT token and user info")
public class AuthResponse {

  @Schema(description = "JWT access token")
  private final String token;

  @Schema(description = "User information")
  private final UserResponse user;
}
