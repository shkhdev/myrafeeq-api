package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "City information")
public class CityResponse {

  @Schema(description = "City ID", example = "tashkent")
  private final String id;

  @Schema(description = "City name", example = "Tashkent")
  private final String name;

  @Schema(description = "Country code", example = "UZ")
  private final String country;

  @Schema(description = "Latitude", example = "41.2995")
  private final Double latitude;

  @Schema(description = "Longitude", example = "69.2401")
  private final Double longitude;

  @Schema(description = "IANA timezone", example = "Asia/Tashkent")
  private final String timezone;

  @Schema(description = "Default calculation method", example = "MBOUZ")
  private final String defaultMethod;

  @Schema(description = "Default madhab", example = "HANAFI")
  private final String defaultMadhab;
}
