package uz.myrafeeq.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a city")
public class UpdateCityRequest {

  @NotBlank
  @Size(max = 255)
  @Schema(description = "City name in English", example = "Tashkent")
  private String name;

  @NotNull
  @DecimalMin("-90")
  @DecimalMax("90")
  @Schema(description = "Geographic latitude", example = "41.2995")
  private Double latitude;

  @NotNull
  @DecimalMin("-180")
  @DecimalMax("180")
  @Schema(description = "Geographic longitude", example = "69.2401")
  private Double longitude;

  @NotBlank
  @Size(max = 100)
  @Schema(description = "IANA timezone identifier", example = "Asia/Tashkent")
  private String timezone;
}
