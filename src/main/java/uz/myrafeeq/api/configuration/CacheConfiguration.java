package uz.myrafeeq.api.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfiguration {

  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(
        List.of(
            buildCache("prayerTimesByLocation", 5000, Duration.ofHours(1)),
            buildCache("citySearch", 1000, Duration.ofHours(24)),
            buildCache("nearestCity", 2000, Duration.ofHours(24)),
            buildCache("userTimezone", 10000, Duration.ofHours(1)),
            buildCache("prayerStats", 10000, Duration.ofMinutes(5))));
    return cacheManager;
  }

  private CaffeineCache buildCache(String name, int maxSize, Duration ttl) {
    return new CaffeineCache(
        name,
        Caffeine.newBuilder().maximumSize(maxSize).expireAfterWrite(ttl).recordStats().build());
  }
}
