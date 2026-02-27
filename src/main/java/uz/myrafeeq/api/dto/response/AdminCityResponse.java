package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "City information for admin")
public class AdminCityResponse {

  @Schema(description = "City ID", example = "tashkent")
  private final String id;

  @Schema(description = "City name", example = "Tashkent")
  private final String name;

  @Schema(description = "Country code", example = "UZ")
  private final String countryCode;

  @Schema(description = "Country name", example = "Uzbekistan")
  private final String countryName;

  @Schema(description = "Latitude", example = "41.2995")
  private final Double latitude;

  @Schema(description = "Longitude", example = "69.2401")
  private final Double longitude;

  @Schema(description = "IANA timezone", example = "Asia/Tashkent")
  private final String timezone;
}
