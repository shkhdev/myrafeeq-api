package uz.myrafeeq.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uz.myrafeeq.api.enums.CalculationMethod;
import uz.myrafeeq.api.enums.Madhab;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a country")
public class UpdateCountryRequest {

  @NotBlank @Size(max = 100) @Schema(description = "Country name in English", example = "Uzbekistan")
  private String name;

  @Schema(description = "Default prayer calculation method", example = "MBOUZ")
  private CalculationMethod defaultMethod;

  @Schema(description = "Default madhab", example = "HANAFI")
  private Madhab defaultMadhab;
}
