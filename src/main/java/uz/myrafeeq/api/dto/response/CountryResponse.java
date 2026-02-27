package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Country information")
public class CountryResponse {

  @Schema(description = "ISO 3166-1 alpha-2 country code", example = "UZ")
  private final String code;

  @Schema(description = "Country name", example = "Uzbekistan")
  private final String name;

  @Schema(description = "Default calculation method", example = "MBOUZ")
  private final String defaultMethod;

  @Schema(description = "Default madhab", example = "HANAFI")
  private final String defaultMadhab;
}
