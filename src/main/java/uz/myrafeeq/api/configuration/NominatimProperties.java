package uz.myrafeeq.api.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import uz.myrafeeq.api.enums.CalculationMethod;
import uz.myrafeeq.api.enums.Madhab;

@Getter
@AllArgsConstructor
@ConfigurationProperties("myrafeeq.nominatim")
public class NominatimProperties {

  private final String url;
  private final String userAgent;
  private final double maxDistanceKm;
  private final CalculationMethod defaultMethod;
  private final Madhab defaultMadhab;
}
