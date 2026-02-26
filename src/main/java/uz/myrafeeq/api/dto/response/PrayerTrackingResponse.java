package uz.myrafeeq.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Prayer tracking data grouped by date and prayer")
public class PrayerTrackingResponse {

  @Schema(description = "Tracking data: date -> prayer -> completed")
  private final Map<String, Map<String, Boolean>> tracking;
}
