package uz.myrafeeq.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uz.myrafeeq.api.enums.PrayerName;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to toggle prayer completion status")
public class TogglePrayerRequest {

  @NotNull
  @Schema(description = "Date of the prayer", example = "2026-02-24")
  private LocalDate date;

  @NotNull
  @Schema(description = "Prayer name", example = "FAJR")
  private PrayerName prayer;

  @NotNull
  @Schema(description = "Whether the prayer was performed", example = "true")
  private Boolean prayed;
}
