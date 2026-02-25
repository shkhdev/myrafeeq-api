package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;

@Builder
@Schema(description = "Response after toggling prayer status")
public record TogglePrayerResponse(
    @Schema(description = "Date of the prayer", example = "2026-02-24") LocalDate date,
    @Schema(description = "Prayer name", example = "FAJR") String prayer,
    @Schema(description = "Whether the prayer was performed", example = "true") Boolean prayed,
    @Schema(description = "Timestamp when the toggle occurred") Instant toggledAt) {}
