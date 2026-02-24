package uz.myrafeeq.api.service.city;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import uz.myrafeeq.api.dto.response.CityResponse;
import uz.myrafeeq.api.dto.response.CitySearchResponse;
import uz.myrafeeq.api.dto.response.NearestCityResponse;
import uz.myrafeeq.api.entity.CityEntity;
import uz.myrafeeq.api.entity.CountryEntity;
import uz.myrafeeq.api.exception.CityNotFoundException;
import uz.myrafeeq.api.mapper.CityMapper;
import uz.myrafeeq.api.repository.CityRepository;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

  @Mock private CityRepository cityRepository;

  @Mock private CityMapper cityMapper;

  @InjectMocks private CityService cityService;

  private static CountryEntity buildCountry() {
    return CountryEntity.builder().code("UZ").name("Uzbekistan").build();
  }

  @Test
  void searchCitiesReturnsMappedResults() {
    CityEntity tashkent =
        CityEntity.builder()
            .id("tashkent")
            .name("Tashkent")
            .country(buildCountry())
            .latitude(41.2995)
            .longitude(69.2401)
            .timezone("Asia/Tashkent")
            .build();

    CityResponse expectedResponse =
        CityResponse.builder()
            .id("tashkent")
            .name("Tashkent")
            .country("UZ")
            .latitude(41.2995)
            .longitude(69.2401)
            .build();

    when(cityRepository.searchByName(eq("tash"), any(Pageable.class)))
        .thenReturn(List.of(tashkent));
    when(cityMapper.toCityResponse(tashkent)).thenReturn(expectedResponse);

    CitySearchResponse result = cityService.searchCities("tash", 10);

    assertThat(result.cities()).hasSize(1);
    assertThat(result.cities().getFirst().id()).isEqualTo("tashkent");
    assertThat(result.cities().getFirst().name()).isEqualTo("Tashkent");
    verify(cityRepository).searchByName(eq("tash"), any(Pageable.class));
    verify(cityMapper).toCityResponse(tashkent);
  }

  @Test
  void searchCitiesEmptyResults() {
    when(cityRepository.searchByName(eq("xyz"), any(Pageable.class)))
        .thenReturn(Collections.emptyList());

    CitySearchResponse result = cityService.searchCities("xyz", 10);

    assertThat(result.cities()).isEmpty();
  }

  @Test
  void findNearestCityReturnsResult() {
    CityEntity city =
        CityEntity.builder()
            .id("tashkent")
            .name("Tashkent")
            .country(buildCountry())
            .latitude(41.2995)
            .longitude(69.2401)
            .timezone("Asia/Tashkent")
            .build();

    CityResponse cityResponse =
        CityResponse.builder()
            .id("tashkent")
            .name("Tashkent")
            .country("UZ")
            .latitude(41.2995)
            .longitude(69.2401)
            .build();

    when(cityRepository.findNearestCity(41.3, 69.3)).thenReturn(city);
    when(cityMapper.toCityResponse(city)).thenReturn(cityResponse);

    NearestCityResponse result = cityService.findNearestCity(41.3, 69.3);

    assertThat(result.city()).isNotNull();
    assertThat(result.city().id()).isEqualTo("tashkent");
    assertThat(result.distanceKm()).isNotNull();
    verify(cityRepository).findNearestCity(41.3, 69.3);
  }

  @Test
  void findNearestCityThrowsWhenNull() {
    when(cityRepository.findNearestCity(0.0, 0.0)).thenReturn(null);

    assertThatThrownBy(() -> cityService.findNearestCity(0.0, 0.0))
        .isInstanceOf(CityNotFoundException.class)
        .hasMessageContaining("No cities found near coordinates");
  }

  @Test
  void findNearestCityCalculatesDistance() {
    CityEntity city =
        CityEntity.builder()
            .id("samarkand")
            .name("Samarkand")
            .country(buildCountry())
            .latitude(39.6542)
            .longitude(66.9597)
            .timezone("Asia/Samarkand")
            .build();

    CityResponse cityResponse =
        CityResponse.builder()
            .id("samarkand")
            .name("Samarkand")
            .country("UZ")
            .latitude(39.6542)
            .longitude(66.9597)
            .build();

    when(cityRepository.findNearestCity(41.2995, 69.2401)).thenReturn(city);
    when(cityMapper.toCityResponse(city)).thenReturn(cityResponse);

    NearestCityResponse result = cityService.findNearestCity(41.2995, 69.2401);

    assertThat(result.distanceKm()).isGreaterThan(0.0);
    assertThat(result.distanceKm()).isGreaterThan(100.0);
  }
}
