package uz.myrafeeq.api.mapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import uz.myrafeeq.api.dto.response.PrayerTrackingResponse;
import uz.myrafeeq.api.entity.PrayerTrackingEntity;

@Component
public class PrayerTrackingMapper {

  public PrayerTrackingResponse toTrackingResponse(List<PrayerTrackingEntity> entities) {
    Map<String, Map<String, Boolean>> tracking = new LinkedHashMap<>();

    for (PrayerTrackingEntity entity : entities) {
      String dateKey = entity.getPrayerDate().toString();
      tracking
          .computeIfAbsent(dateKey, _ -> new LinkedHashMap<>())
          .put(entity.getPrayerName().name(), entity.getPrayed());
    }

    return PrayerTrackingResponse.builder().tracking(tracking).build();
  }
}
