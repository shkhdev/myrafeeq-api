package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "City information")
public record CityResponse(
    @Schema(description = "City ID", example = "tashkent") String id,
    @Schema(description = "Localized city name", example = "Tashkent") String name,
    @Schema(description = "Country code", example = "UZ") String country,
    @Schema(description = "Latitude", example = "41.2995") Double latitude,
    @Schema(description = "Longitude", example = "69.2401") Double longitude) {}
