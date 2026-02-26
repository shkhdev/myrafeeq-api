package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Nearest city to given coordinates")
public class NearestCityResponse {

  @Schema(description = "Nearest city")
  private final CityResponse city;

  @Schema(description = "Distance in kilometers", example = "12.5")
  private final Double distanceKm;
}
