package uz.myrafeeq.api.service.city;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.myrafeeq.api.configuration.NominatimProperties;
import uz.myrafeeq.api.dto.response.CityResponse;
import uz.myrafeeq.api.dto.response.CitySearchResponse;
import uz.myrafeeq.api.dto.response.NearestCityResponse;
import uz.myrafeeq.api.entity.CityEntity;
import uz.myrafeeq.api.entity.CountryEntity;
import uz.myrafeeq.api.exception.CityNotFoundException;
import uz.myrafeeq.api.mapper.CityMapper;
import uz.myrafeeq.api.repository.CityRepository;
import uz.myrafeeq.api.repository.CountryRepository;
import uz.myrafeeq.api.service.city.NominatimClient.NominatimAddress;
import uz.myrafeeq.api.service.city.NominatimClient.NominatimPlace;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityService {

  private static final double EARTH_RADIUS_KM = 6371.0;

  private final CityRepository cityRepository;
  private final CityMapper cityMapper;
  private final NominatimClient nominatimClient;
  private final TimeZoneResolver timeZoneResolver;
  private final CountryRepository countryRepository;
  private final NominatimProperties nominatimProperties;

  @Transactional(readOnly = true)
  @Cacheable(
      value = "citySearch",
      key = "#query.toLowerCase() + '-' + #limit",
      unless = "#result.cities.isEmpty()")
  public CitySearchResponse searchCities(String query, int limit) {
    List<CityEntity> dbCities = cityRepository.searchByName(query, Pageable.ofSize(limit));

    if (!dbCities.isEmpty()) {
      List<CityResponse> responses = dbCities.stream().map(cityMapper::toCityResponse).toList();
      return CitySearchResponse.builder().cities(responses).build();
    }

    List<NominatimPlace> places = nominatimClient.searchCities(query, limit);
    if (places.isEmpty()) {
      return CitySearchResponse.builder().cities(List.of()).build();
    }

    Map<String, CityResponse> deduped = new LinkedHashMap<>();
    for (NominatimPlace place : places) {
      double placeLat = Double.parseDouble(place.lat());
      double placeLon = Double.parseDouble(place.lon());

      CityEntity nearest = cityRepository.findNearestCity(placeLat, placeLon);
      if (nearest != null) {
        double distance =
            haversineDistance(placeLat, placeLon, nearest.getLatitude(), nearest.getLongitude());
        if (distance < nominatimProperties.getMaxDistanceKm()) {
          deduped.putIfAbsent(nearest.getId(), cityMapper.toCityResponse(nearest));
          continue;
        }
      }

      CityResponse response = buildCityResponseFromNominatim(place);
      deduped.putIfAbsent(response.getId(), response);
    }

    List<CityResponse> responses = List.copyOf(deduped.values());
    return CitySearchResponse.builder().cities(responses).build();
  }

  @Transactional
  public NearestCityResponse findNearestCity(double lat, double lon) {
    CityEntity existing = cityRepository.findNearestCity(lat, lon);

    if (existing != null) {
      double distance =
          haversineDistance(lat, lon, existing.getLatitude(), existing.getLongitude());
      if (distance < nominatimProperties.getMaxDistanceKm()) {
        return NearestCityResponse.builder()
            .city(cityMapper.toCityResponse(existing))
            .distanceKm(Math.round(distance * 100.0) / 100.0)
            .build();
      }
    }

    Optional<NominatimPlace> reversed = nominatimClient.reverse(lat, lon);
    if (reversed.isPresent()) {
      CityEntity created = createCityFromNominatim(reversed.get());
      if (created != null) {
        double distance =
            haversineDistance(lat, lon, created.getLatitude(), created.getLongitude());
        return NearestCityResponse.builder()
            .city(cityMapper.toCityResponse(created))
            .distanceKm(Math.round(distance * 100.0) / 100.0)
            .build();
      }
    }

    if (existing != null) {
      double distance =
          haversineDistance(lat, lon, existing.getLatitude(), existing.getLongitude());
      return NearestCityResponse.builder()
          .city(cityMapper.toCityResponse(existing))
          .distanceKm(Math.round(distance * 100.0) / 100.0)
          .build();
    }

    throw new CityNotFoundException("No cities found near coordinates: " + lat + ", " + lon);
  }

  @Transactional
  public CityEntity getOrCreateCity(String cityId) {
    Optional<CityEntity> existing = cityRepository.findById(cityId);
    if (existing.isPresent()) {
      return existing.get();
    }

    Optional<NominatimPlace> place = nominatimClient.lookup(cityId);
    if (place.isPresent()) {
      CityEntity created = createCityFromNominatim(place.get());
      if (created != null) {
        return created;
      }
    }

    throw new CityNotFoundException("City not found: " + cityId);
  }

  private CityResponse buildCityResponseFromNominatim(NominatimPlace place) {
    NominatimAddress address = place.address();
    String cityName = resolveCityName(address, place.name());
    String id = generateOsmId(place.osmType(), place.osmId());
    double lat = Double.parseDouble(place.lat());
    double lon = Double.parseDouble(place.lon());
    String timezone = timeZoneResolver.resolve(lat, lon);

    String countryCode =
        address != null && address.countryCode() != null
            ? address.countryCode().toUpperCase()
            : "XX";

    String defaultMethod = nominatimProperties.getDefaultMethod().name();
    String defaultMadhab = nominatimProperties.getDefaultMadhab().name();

    Optional<CountryEntity> country = countryRepository.findById(countryCode);
    if (country.isPresent()) {
      defaultMethod = country.get().getDefaultMethod().name();
      defaultMadhab = country.get().getDefaultMadhab().name();
    }

    return CityResponse.builder()
        .id(id)
        .name(cityName)
        .country(countryCode)
        .latitude(lat)
        .longitude(lon)
        .timezone(timezone)
        .defaultMethod(defaultMethod)
        .defaultMadhab(defaultMadhab)
        .build();
  }

  private CityEntity createCityFromNominatim(NominatimPlace place) {
    try {
      NominatimAddress address = place.address();
      String cityName = resolveCityName(address, place.name());
      String id = generateOsmId(place.osmType(), place.osmId());

      Optional<CityEntity> existingCity = cityRepository.findById(id);
      if (existingCity.isPresent()) {
        return existingCity.get();
      }

      double lat = Double.parseDouble(place.lat());
      double lon = Double.parseDouble(place.lon());

      String timezone = timeZoneResolver.resolve(lat, lon);

      String countryCode =
          address != null && address.countryCode() != null
              ? address.countryCode().toUpperCase()
              : "XX";
      String countryName =
          address != null && address.country() != null ? address.country() : "Unknown";

      CountryEntity country =
          countryRepository
              .findById(countryCode)
              .orElseGet(
                  () ->
                      countryRepository.save(
                          CountryEntity.builder()
                              .code(countryCode)
                              .name(countryName)
                              .defaultMethod(nominatimProperties.getDefaultMethod())
                              .defaultMadhab(nominatimProperties.getDefaultMadhab())
                              .build()));

      CityEntity city =
          CityEntity.builder()
              .id(id)
              .name(cityName)
              .country(country)
              .latitude(lat)
              .longitude(lon)
              .timezone(timezone)
              .build();

      return cityRepository.save(city);
    } catch (Exception e) {
      log.warn("Failed to create city from Nominatim place {}: {}", place.name(), e.getMessage());
      return null;
    }
  }

  private String resolveCityName(NominatimAddress address, String fallbackName) {
    if (address != null) {
      if (address.city() != null) return address.city();
      if (address.town() != null) return address.town();
      if (address.village() != null) return address.village();
    }
    return fallbackName;
  }

  private String generateOsmId(String osmType, Long osmId) {
    if (osmType == null || osmId == null) {
      return "N" + System.currentTimeMillis();
    }
    String prefix =
        switch (osmType.toLowerCase()) {
          case "relation" -> "R";
          case "way" -> "W";
          case "node" -> "N";
          default -> osmType.substring(0, 1).toUpperCase();
        };
    return prefix + osmId;
  }

  private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS_KM * c;
  }
}
