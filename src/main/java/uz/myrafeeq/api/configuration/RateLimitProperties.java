package uz.myrafeeq.api.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties("myrafeeq.rate-limit")
public class RateLimitProperties {

  private final boolean enabled;
  private final int requestsPerMinute;
  private final int authRequestsPerMinute;
}
