package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error response")
public record ErrorResponse(Error error) {

  public static ErrorResponse of(String code, String message) {
    return new ErrorResponse(new Error(code, message));
  }

  @Schema(description = "Error details")
  public record Error(
      @Schema(description = "Machine-readable error code", example = "VALIDATION_ERROR")
          String code,
      @Schema(
              description = "Human-readable error message",
              example = "identifier: must not be blank")
          String message) {}
}
