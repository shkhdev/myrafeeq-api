package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Authentication response with JWT token and user info")
public record AuthResponse(
    @Schema(description = "JWT access token") String token,
    @Schema(description = "User information") UserResponse user) {}
