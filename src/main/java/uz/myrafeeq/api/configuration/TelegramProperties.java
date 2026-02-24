package uz.myrafeeq.api.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("myrafeeq.telegram")
public record TelegramProperties(String botToken, Duration authDataTtl) {}
