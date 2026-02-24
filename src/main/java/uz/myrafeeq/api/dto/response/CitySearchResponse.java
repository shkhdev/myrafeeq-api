package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "City search results")
public record CitySearchResponse(
    @Schema(description = "Matching cities") List<CityResponse> cities) {}
