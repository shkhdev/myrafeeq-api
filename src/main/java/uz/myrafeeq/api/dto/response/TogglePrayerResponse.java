package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Response after toggling prayer status")
public class TogglePrayerResponse {

  @Schema(description = "Date of the prayer", example = "2026-02-24")
  private final LocalDate date;

  @Schema(description = "Prayer name", example = "FAJR")
  private final String prayer;

  @Schema(description = "Whether the prayer was performed", example = "true")
  private final Boolean prayed;

  @Schema(description = "Timestamp when the toggle occurred")
  private final Instant toggledAt;
}
