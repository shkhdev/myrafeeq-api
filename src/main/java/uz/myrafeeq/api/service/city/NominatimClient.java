package uz.myrafeeq.api.service.city;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uz.myrafeeq.api.configuration.NominatimProperties;

@Slf4j
@Component
public class NominatimClient {

  private static final Set<String> CITY_ADDRESS_TYPES = Set.of("city", "town", "village", "hamlet");

  private final RestClient restClient;
  private final Semaphore semaphore = new Semaphore(1);
  private volatile long lastRequestTime = 0;

  public NominatimClient(NominatimProperties properties, RestClient.Builder restClientBuilder) {
    this.restClient =
        restClientBuilder
            .baseUrl(properties.getUrl())
            .defaultHeader("User-Agent", properties.getUserAgent())
            .build();
  }

  @Cacheable(
      value = "nominatimSearch",
      key = "#query.toLowerCase() + '-' + #limit",
      unless = "#result.isEmpty()")
  public List<NominatimPlace> searchCities(String query, int limit) {
    try {
      throttle();
      List<NominatimPlace> results =
          restClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/search")
                          .queryParam("q", query)
                          .queryParam("featuretype", "settlement")
                          .queryParam("format", "json")
                          .queryParam("limit", limit)
                          .queryParam("addressdetails", 1)
                          .queryParam("accept-language", "en")
                          .build())
              .retrieve()
              .body(new ParameterizedTypeReference<List<NominatimPlace>>() {});
      if (results == null) {
        return List.of();
      }
      return results.stream()
          .filter(p -> p.addresstype() != null && CITY_ADDRESS_TYPES.contains(p.addresstype()))
          .toList();
    } catch (Exception e) {
      log.warn("Nominatim search failed for query '{}': {}", query, e.getMessage());
      return List.of();
    }
  }

  @Cacheable(value = "nominatimLookup", key = "#osmId", unless = "#result == null")
  public Optional<NominatimPlace> lookup(String osmId) {
    try {
      throttle();
      List<NominatimPlace> results =
          restClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/lookup")
                          .queryParam("osm_ids", osmId)
                          .queryParam("format", "json")
                          .queryParam("addressdetails", 1)
                          .queryParam("accept-language", "en")
                          .build())
              .retrieve()
              .body(new ParameterizedTypeReference<List<NominatimPlace>>() {});
      if (results == null || results.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(results.getFirst());
    } catch (Exception e) {
      log.warn("Nominatim lookup failed for {}: {}", osmId, e.getMessage());
      return Optional.empty();
    }
  }

  @Cacheable(
      value = "nominatimReverse",
      key = "T(Math).round(#lat * 1000) + ',' + T(Math).round(#lon * 1000)")
  public Optional<NominatimPlace> reverse(double lat, double lon) {
    try {
      throttle();
      NominatimPlace place =
          restClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/reverse")
                          .queryParam("lat", lat)
                          .queryParam("lon", lon)
                          .queryParam("zoom", 10)
                          .queryParam("format", "json")
                          .queryParam("addressdetails", 1)
                          .queryParam("accept-language", "en")
                          .build())
              .retrieve()
              .body(NominatimPlace.class);
      return Optional.ofNullable(place);
    } catch (Exception e) {
      log.warn("Nominatim reverse geocoding failed for ({}, {}): {}", lat, lon, e.getMessage());
      return Optional.empty();
    }
  }

  private void throttle() throws InterruptedException {
    semaphore.acquire();
    try {
      long elapsed = System.currentTimeMillis() - lastRequestTime;
      if (elapsed < 1000) {
        Thread.sleep(1000 - elapsed);
      }
      lastRequestTime = System.currentTimeMillis();
    } finally {
      semaphore.release();
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record NominatimPlace(
      String lat,
      String lon,
      String name,
      @JsonProperty("osm_id") Long osmId,
      @JsonProperty("osm_type") String osmType,
      String addresstype,
      NominatimAddress address) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record NominatimAddress(
      String city,
      String town,
      String village,
      String country,
      @JsonProperty("country_code") String countryCode) {}
}
