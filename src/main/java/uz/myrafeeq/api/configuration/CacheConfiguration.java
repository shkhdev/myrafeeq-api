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
            buildCache("citySearch", 5000, Duration.ofDays(30)),
            buildCache("nearestCity", 5000, Duration.ofDays(30)),
            buildCache("userTimezone", 10000, Duration.ofHours(1)),
            buildCache("prayerStats", 10000, Duration.ofMinutes(5)),
            buildCache("nominatimSearch", 5000, Duration.ofDays(30)),
            buildCache("nominatimLookup", 5000, Duration.ofDays(30)),
            buildCache("nominatimReverse", 5000, Duration.ofDays(30))));
    return cacheManager;
  }

  private CaffeineCache buildCache(String name, int maxSize, Duration ttl) {
    return new CaffeineCache(
        name,
        Caffeine.newBuilder().maximumSize(maxSize).expireAfterWrite(ttl).recordStats().build());
  }
}
