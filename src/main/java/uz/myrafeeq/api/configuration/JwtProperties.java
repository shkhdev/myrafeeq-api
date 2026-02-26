package uz.myrafeeq.api.configuration;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties("myrafeeq.jwt")
public class JwtProperties {

  private final String secret;
  private final Duration ttl;
}
