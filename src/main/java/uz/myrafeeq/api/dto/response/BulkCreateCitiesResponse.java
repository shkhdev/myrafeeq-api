package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Result of bulk city creation")
public class BulkCreateCitiesResponse {

  @Schema(description = "Number of cities created", example = "42")
  private final int created;

  @Schema(description = "Created cities")
  private final List<AdminCityResponse> cities;
}
