package uz.myrafeeq.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to bulk create cities")
public class BulkCreateCitiesRequest {

  @NotEmpty @Size(max = 500) @Valid @Schema(description = "List of cities to create")
  private List<CreateCityRequest> cities;
}
