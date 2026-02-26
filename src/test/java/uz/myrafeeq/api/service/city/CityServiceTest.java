package uz.myrafeeq.api.service.city;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.myrafeeq.api.dto.response.CityResponse;
import uz.myrafeeq.api.dto.response.CitySearchResponse;
import uz.myrafeeq.api.dto.response.NearestCityResponse;
import uz.myrafeeq.api.entity.CityEntity;
import uz.myrafeeq.api.entity.CountryEntity;
import uz.myrafeeq.api.enums.CalculationMethod;
import uz.myrafeeq.api.enums.Madhab;
import uz.myrafeeq.api.exception.CityNotFoundException;
import uz.myrafeeq.api.mapper.CityMapper;
import uz.myrafeeq.api.repository.CityRepository;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

  @Mock private CityRepository cityRepository;
  @Mock private CityMapper cityMapper;
  @InjectMocks private CityService cityService;

  @Test
  void should_returnCities_when_searchByName() {
    CityEntity city = buildCityEntity("tashkent", "Tashkent", 41.2995, 69.2401);
    CityResponse cityResponse = buildCityResponse("tashkent", "Tashkent");

    given(cityRepository.searchByName(eq("Tashkent"), any())).willReturn(List.of(city));
    given(cityMapper.toCityResponse(city)).willReturn(cityResponse);

    CitySearchResponse result = cityService.searchCities("Tashkent", 10);

    assertThat(result.cities()).hasSize(1);
    assertThat(result.cities().getFirst().name()).isEqualTo("Tashkent");
  }

  @Test
  void should_returnEmptyList_when_noMatchingCities() {
    given(cityRepository.searchByName(eq("Unknown"), any())).willReturn(List.of());

    CitySearchResponse result = cityService.searchCities("Unknown", 10);

    assertThat(result.cities()).isEmpty();
  }

  @Test
  void should_returnMultipleCities_when_multipleMatches() {
    CityEntity city1 = buildCityEntity("tashkent", "Tashkent", 41.2995, 69.2401);
    CityEntity city2 = buildCityEntity("samarkand", "Samarkand", 39.6542, 66.9597);
    CityResponse resp1 = buildCityResponse("tashkent", "Tashkent");
    CityResponse resp2 = buildCityResponse("samarkand", "Samarkand");

    given(cityRepository.searchByName(eq("a"), any())).willReturn(List.of(city1, city2));
    given(cityMapper.toCityResponse(city1)).willReturn(resp1);
    given(cityMapper.toCityResponse(city2)).willReturn(resp2);

    CitySearchResponse result = cityService.searchCities("a", 10);

    assertThat(result.cities()).hasSize(2);
    assertThat(result.cities())
        .extracting(CityResponse::name)
        .containsExactly("Tashkent", "Samarkand");
  }

  @Test
  void should_returnNearestCity_when_validCoordinates() {
    CityEntity city = buildCityEntity("tashkent", "Tashkent", 41.2995, 69.2401);
    CityResponse cityResponse = buildCityResponse("tashkent", "Tashkent");

    given(cityRepository.findNearestCity(41.3, 69.3)).willReturn(city);
    given(cityMapper.toCityResponse(city)).willReturn(cityResponse);

    NearestCityResponse result = cityService.findNearestCity(41.3, 69.3);

    assertThat(result.city().name()).isEqualTo("Tashkent");
    assertThat(result.distanceKm()).isGreaterThanOrEqualTo(0.0);
  }

  @Test
  void should_throwCityNotFound_when_noCitiesExist() {
    given(cityRepository.findNearestCity(0.0, 0.0)).willReturn(null);

    assertThatThrownBy(() -> cityService.findNearestCity(0.0, 0.0))
        .isInstanceOf(CityNotFoundException.class)
        .hasMessageContaining("No cities found");
  }

  @Test
  void should_returnZeroDistance_when_exactCoordinatesMatch() {
    CityEntity city = buildCityEntity("tashkent", "Tashkent", 41.2995, 69.2401);
    CityResponse cityResponse = buildCityResponse("tashkent", "Tashkent");

    given(cityRepository.findNearestCity(41.2995, 69.2401)).willReturn(city);
    given(cityMapper.toCityResponse(city)).willReturn(cityResponse);

    NearestCityResponse result = cityService.findNearestCity(41.2995, 69.2401);

    assertThat(result.distanceKm()).isEqualTo(0.0);
  }

  @Test
  void should_calculateReasonableDistance_when_knownCities() {
    // Tashkent to a point ~10km away
    CityEntity city = buildCityEntity("tashkent", "Tashkent", 41.2995, 69.2401);
    CityResponse cityResponse = buildCityResponse("tashkent", "Tashkent");

    given(cityRepository.findNearestCity(41.4, 69.3)).willReturn(city);
    given(cityMapper.toCityResponse(city)).willReturn(cityResponse);

    NearestCityResponse result = cityService.findNearestCity(41.4, 69.3);

    assertThat(result.distanceKm()).isBetween(5.0, 20.0);
  }

  private CityEntity buildCityEntity(String id, String name, double lat, double lon) {
    CountryEntity country =
        CountryEntity.builder()
            .code("UZ")
            .name("Uzbekistan")
            .defaultMethod(CalculationMethod.MBOUZ)
            .defaultMadhab(Madhab.HANAFI)
            .build();
    return CityEntity.builder()
        .id(id)
        .name(name)
        .country(country)
        .latitude(lat)
        .longitude(lon)
        .timezone("Asia/Tashkent")
        .build();
  }

  private CityResponse buildCityResponse(String id, String name) {
    return CityResponse.builder()
        .id(id)
        .name(name)
        .country("UZ")
        .latitude(41.2995)
        .longitude(69.2401)
        .timezone("Asia/Tashkent")
        .defaultMethod("MBOUZ")
        .defaultMadhab("HANAFI")
        .build();
  }
}
