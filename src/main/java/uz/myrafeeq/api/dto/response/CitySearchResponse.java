package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "City search results")
public class CitySearchResponse {

  @Schema(description = "Matching cities")
  private final List<CityResponse> cities;
}
