package uz.myrafeeq.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uz.myrafeeq.api.enums.CalculationMethod;
import uz.myrafeeq.api.enums.Madhab;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a country")
public class CreateCountryRequest {

  @NotBlank @Size(min = 2, max = 2) @Pattern(regexp = "^[A-Z]{2}$", message = "Must be a 2-letter uppercase ISO country code") @Schema(description = "ISO 3166-1 alpha-2 country code", example = "UZ")
  private String code;

  @NotBlank @Size(max = 100) @Schema(description = "Country name in English", example = "Uzbekistan")
  private String name;

  @Schema(description = "Default prayer calculation method", example = "MBOUZ")
  private CalculationMethod defaultMethod;

  @Schema(description = "Default madhab", example = "HANAFI")
  private Madhab defaultMadhab;
}
