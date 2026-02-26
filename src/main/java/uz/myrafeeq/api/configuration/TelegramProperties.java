package uz.myrafeeq.api.configuration;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties("myrafeeq.telegram")
public class TelegramProperties {

  private final String botToken;
  private final Duration authDataTtl;
}
