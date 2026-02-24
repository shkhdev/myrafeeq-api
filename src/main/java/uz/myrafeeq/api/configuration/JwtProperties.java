package uz.myrafeeq.api.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("myrafeeq.jwt")
public record JwtProperties(String secret, Duration ttl) {}
