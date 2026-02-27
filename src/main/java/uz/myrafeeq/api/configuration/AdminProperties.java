package uz.myrafeeq.api.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties("myrafeeq.admin")
public class AdminProperties {

  private final String apiKey;
}
