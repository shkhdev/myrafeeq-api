package uz.myrafeeq.api.configuration;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("myrafeeq.cors")
public record CorsProperties(List<String> allowedOrigins) {}
