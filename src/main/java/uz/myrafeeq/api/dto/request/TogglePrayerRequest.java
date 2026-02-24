package uz.myrafeeq.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import uz.myrafeeq.api.enums.PrayerName;

@Schema(description = "Request to toggle prayer completion status")
public record TogglePrayerRequest(
    @NotNull @Schema(description = "Date of the prayer", example = "2026-02-24") LocalDate date,
    @NotNull @Schema(description = "Prayer name", example = "FAJR") PrayerName prayer,
    @NotNull @Schema(description = "Whether the prayer was performed", example = "true")
        Boolean prayed) {}
