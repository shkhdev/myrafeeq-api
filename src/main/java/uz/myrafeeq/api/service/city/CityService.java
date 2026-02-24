package uz.myrafeeq.api.service.city;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.myrafeeq.api.dto.response.CityResponse;
import uz.myrafeeq.api.dto.response.CitySearchResponse;
import uz.myrafeeq.api.dto.response.NearestCityResponse;
import uz.myrafeeq.api.entity.CityEntity;
import uz.myrafeeq.api.exception.CityNotFoundException;
import uz.myrafeeq.api.mapper.CityMapper;
import uz.myrafeeq.api.repository.CityRepository;

@Service
@RequiredArgsConstructor
public class CityService {

  private static final double EARTH_RADIUS_KM = 6371.0;

  private final CityRepository cityRepository;
  private final CityMapper cityMapper;

  @Transactional(readOnly = true)
  public CitySearchResponse searchCities(String query, int limit) {
    List<CityEntity> cities = cityRepository.searchByName(query, Pageable.ofSize(limit));

    List<CityResponse> responses = cities.stream().map(cityMapper::toCityResponse).toList();

    return CitySearchResponse.builder().cities(responses).build();
  }

  @Transactional(readOnly = true)
  public NearestCityResponse findNearestCity(double lat, double lon) {
    CityEntity city = cityRepository.findNearestCity(lat, lon);

    if (city == null) {
      throw new CityNotFoundException("No cities found near coordinates: " + lat + ", " + lon);
    }

    double distance = haversineDistance(lat, lon, city.getLatitude(), city.getLongitude());

    return NearestCityResponse.builder()
        .city(cityMapper.toCityResponse(city))
        .distanceKm(Math.round(distance * 100.0) / 100.0)
        .build();
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
