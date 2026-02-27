package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "System statistics")
public class AdminStatsResponse {

  @Schema(description = "Total registered users", example = "1234")
  private final long totalUsers;

  @Schema(description = "Users who completed onboarding", example = "987")
  private final long onboardedUsers;

  @Schema(description = "Total countries in database", example = "15")
  private final long totalCountries;

  @Schema(description = "Total cities in database", example = "245")
  private final long totalCities;

  @Schema(description = "Total prayer tracking records", example = "56789")
  private final long totalTrackingRecords;
}
