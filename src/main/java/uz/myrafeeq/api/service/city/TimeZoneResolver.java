package uz.myrafeeq.api.service.city;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import us.dustinj.timezonemap.TimeZoneMap;

@Slf4j
@Component
public class TimeZoneResolver {

  private TimeZoneMap timeZoneMap;

  @PostConstruct
  void init() {
    log.info("Loading timezone map data...");
    timeZoneMap = TimeZoneMap.forEverywhere();
    log.info("Timezone map loaded");
  }

  public String resolve(double lat, double lon) {
    us.dustinj.timezonemap.TimeZone tz = timeZoneMap.getOverlappingTimeZone(lat, lon);
    if (tz != null) {
      return tz.getZoneId();
    }
    log.warn("No timezone found for ({}, {}), falling back to UTC", lat, lon);
    return "UTC";
  }
}
