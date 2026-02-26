package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "Standard error response")
public record ErrorResponse(Error error) {

  public static ErrorResponse of(String code, String message, Instant timestamp, String path) {
    return new ErrorResponse(new Error(code, message, timestamp, path, null));
  }

  public static ErrorResponse ofValidation(
      List<FieldError> fieldErrors, Instant timestamp, String path) {
    String message =
        fieldErrors.stream()
            .map(e -> e.field() + ": " + e.message())
            .collect(Collectors.joining("; "));
    return new ErrorResponse(new Error("VALIDATION_ERROR", message, timestamp, path, fieldErrors));
  }

  @Schema(description = "Error details")
  public record Error(
      @Schema(description = "Machine-readable error code", example = "VALIDATION_ERROR")
          String code,
      @Schema(
              description = "Human-readable error message",
              example = "identifier: must not be blank")
          String message,
      @Schema(description = "Timestamp of the error") Instant timestamp,
      @Schema(description = "Request path", example = "/api/v1/prayer-tracking/toggle") String path,
      @Schema(description = "Field-level validation errors") List<FieldError> fieldErrors) {}

  @Schema(description = "Field validation error")
  public record FieldError(
      @Schema(description = "Field name", example = "date") String field,
      @Schema(description = "Error message", example = "must not be null") String message) {}
}
