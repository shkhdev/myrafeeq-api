package uz.myrafeeq.api.configuration;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties("myrafeeq.cors")
public class CorsProperties {

  private final List<String> allowedOrigins;
}
