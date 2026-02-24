package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Nearest city to given coordinates")
public record NearestCityResponse(
    @Schema(description = "Nearest city") CityResponse city,
    @Schema(description = "Distance in kilometers", example = "12.5") Double distanceKm) {}
