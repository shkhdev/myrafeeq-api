package uz.myrafeeq.api.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("myrafeeq.rate-limit")
public record RateLimitProperties(
    boolean enabled, int requestsPerMinute, int authRequestsPerMinute) {}
