package uz.myrafeeq.api.service.city;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.myrafeeq.api.configuration.NominatimProperties;
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
import uz.myrafeeq.api.repository.CountryRepository;
import uz.myrafeeq.api.service.city.NominatimClient.NominatimAddress;
import uz.myrafeeq.api.service.city.NominatimClient.NominatimPlace;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

  @Mock private CityRepository cityRepository;
  @Mock private CityMapper cityMapper;
  @Mock private NominatimClient nominatimClient;
  @Mock private TimeZoneResolver timeZoneResolver;
  @Mock private CountryRepository countryRepository;
  @Mock private NominatimProperties nominatimProperties;
  @InjectMocks private CityService cityService;

  @Test
  void should_returnCities_when_searchByName() {
    CityEntity city = buildCityEntity("tashkent", "Tashkent", 41.2995, 69.2401);
    CityResponse cityResponse = buildCityResponse("tashkent", "Tashkent");

    given(cityRepository.searchByName(eq("Tashkent"), any())).willReturn(List.of(city));
    given(cityMapper.toCityResponse(city)).willReturn(cityResponse);

    CitySearchResponse result = cityService.searchCities("Tashkent", 10);

    assertThat(result.getCities()).hasSize(1);
    assertThat(result.getCities().getFirst().getName()).isEqualTo("Tashkent");
  }

  @Test
  void should_returnEmptyList_when_noMatchingCities() {
    given(cityRepository.searchByName(eq("Unknown"), any())).willReturn(List.of());
    given(nominatimClient.searchCities("Unknown", 10)).willReturn(List.of());

    CitySearchResponse result = cityService.searchCities("Unknown", 10);

    assertThat(result.getCities()).isEmpty();
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

    assertThat(result.getCities()).hasSize(2);
    assertThat(result.getCities())
        .extracting(CityResponse::getName)
        .containsExactly("Tashkent", "Samarkand");
  }

  @Test
  void should_returnNearestCity_when_validCoordinates() {
    CityEntity city = buildCityEntity("tashkent", "Tashkent", 41.2995, 69.2401);
    CityResponse cityResponse = buildCityResponse("tashkent", "Tashkent");

    given(cityRepository.findNearestCity(41.3, 69.3)).willReturn(city);
    given(cityMapper.toCityResponse(city)).willReturn(cityResponse);
    given(nominatimProperties.getMaxDistanceKm()).willReturn(50.0);

    NearestCityResponse result = cityService.findNearestCity(41.3, 69.3);

    assertThat(result.getCity().getName()).isEqualTo("Tashkent");
    assertThat(result.getDistanceKm()).isGreaterThanOrEqualTo(0.0);
  }

  @Test
  void should_throwCityNotFound_when_noCitiesExist() {
    given(cityRepository.findNearestCity(0.0, 0.0)).willReturn(null);
    given(nominatimClient.reverse(0.0, 0.0)).willReturn(Optional.empty());

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
    given(nominatimProperties.getMaxDistanceKm()).willReturn(50.0);

    NearestCityResponse result = cityService.findNearestCity(41.2995, 69.2401);

    assertThat(result.getDistanceKm()).isEqualTo(0.0);
  }

  @Test
  void should_calculateReasonableDistance_when_knownCities() {
    CityEntity city = buildCityEntity("tashkent", "Tashkent", 41.2995, 69.2401);
    CityResponse cityResponse = buildCityResponse("tashkent", "Tashkent");

    given(cityRepository.findNearestCity(41.4, 69.3)).willReturn(city);
    given(cityMapper.toCityResponse(city)).willReturn(cityResponse);
    given(nominatimProperties.getMaxDistanceKm()).willReturn(50.0);

    NearestCityResponse result = cityService.findNearestCity(41.4, 69.3);

    assertThat(result.getDistanceKm()).isBetween(5.0, 20.0);
  }

  @Test
  void should_notCallNominatim_when_dbHasResults() {
    CityEntity city = buildCityEntity("tashkent", "Tashkent", 41.2995, 69.2401);
    CityResponse cityResponse = buildCityResponse("tashkent", "Tashkent");

    given(cityRepository.searchByName(eq("Tashkent"), any())).willReturn(List.of(city));
    given(cityMapper.toCityResponse(city)).willReturn(cityResponse);

    cityService.searchCities("Tashkent", 10);

    verify(nominatimClient, never()).searchCities(any(), eq(10));
  }

  @Test
  void should_fallbackToNominatim_when_dbEmpty() {
    given(cityRepository.searchByName(eq("Ташкент"), any())).willReturn(List.of());

    NominatimPlace place =
        new NominatimPlace(
            "41.2995",
            "69.2401",
            "Tashkent",
            1991790L,
            "relation",
            "city",
            new NominatimAddress("Tashkent", null, null, "Uzbekistan", "uz"));
    given(nominatimClient.searchCities("Ташкент", 10)).willReturn(List.of(place));

    CityEntity nearestCity = buildCityEntity("tashkent", "Tashkent", 41.3, 69.24);
    given(cityRepository.findNearestCity(41.2995, 69.2401)).willReturn(nearestCity);
    given(nominatimProperties.getMaxDistanceKm()).willReturn(50.0);

    CityResponse cityResponse = buildCityResponse("tashkent", "Tashkent");
    given(cityMapper.toCityResponse(nearestCity)).willReturn(cityResponse);

    CitySearchResponse result = cityService.searchCities("Ташкент", 10);

    assertThat(result.getCities()).hasSize(1);
    assertThat(result.getCities().getFirst().getName()).isEqualTo("Tashkent");
  }

  @Test
  void should_deduplicateResults_when_multipleNominatimResultsMapToSameCity() {
    given(cityRepository.searchByName(eq("test"), any())).willReturn(List.of());

    NominatimPlace place1 =
        new NominatimPlace(
            "41.30",
            "69.25",
            "Tashkent",
            1L,
            "relation",
            "city",
            new NominatimAddress("Tashkent", null, null, "Uzbekistan", "uz"));
    NominatimPlace place2 =
        new NominatimPlace(
            "41.31",
            "69.26",
            "Tashkent City",
            2L,
            "relation",
            "city",
            new NominatimAddress("Tashkent", null, null, "Uzbekistan", "uz"));
    given(nominatimClient.searchCities("test", 10)).willReturn(List.of(place1, place2));

    CityEntity nearestCity = buildCityEntity("tashkent", "Tashkent", 41.3, 69.24);
    given(cityRepository.findNearestCity(anyDouble(), anyDouble())).willReturn(nearestCity);
    given(nominatimProperties.getMaxDistanceKm()).willReturn(50.0);

    CityResponse cityResponse = buildCityResponse("tashkent", "Tashkent");
    given(cityMapper.toCityResponse(nearestCity)).willReturn(cityResponse);

    CitySearchResponse result = cityService.searchCities("test", 10);

    assertThat(result.getCities()).hasSize(1);
  }

  @Test
  void should_returnNominatimResponseWithoutSaving_when_noNearbyCityExists() {
    given(cityRepository.searchByName(eq("Reykjavik"), any())).willReturn(List.of());

    NominatimPlace place =
        new NominatimPlace(
            "64.1466",
            "-21.9426",
            "Reykjavik",
            123456L,
            "relation",
            "city",
            new NominatimAddress("Reykjavik", null, null, "Iceland", "is"));
    given(nominatimClient.searchCities("Reykjavik", 10)).willReturn(List.of(place));

    CityEntity distantCity = buildCityEntity("tashkent", "Tashkent", 41.3, 69.24);
    given(cityRepository.findNearestCity(64.1466, -21.9426)).willReturn(distantCity);
    given(nominatimProperties.getMaxDistanceKm()).willReturn(50.0);
    given(nominatimProperties.getDefaultMethod()).willReturn(CalculationMethod.MWL);
    given(nominatimProperties.getDefaultMadhab()).willReturn(Madhab.HANAFI);
    given(timeZoneResolver.resolve(64.1466, -21.9426)).willReturn("Atlantic/Reykjavik");
    given(countryRepository.findById("IS")).willReturn(Optional.empty());

    CitySearchResponse result = cityService.searchCities("Reykjavik", 10);

    assertThat(result.getCities()).hasSize(1);
    assertThat(result.getCities().getFirst().getId()).isEqualTo("R123456");
    assertThat(result.getCities().getFirst().getTimezone()).isEqualTo("Atlantic/Reykjavik");
    verify(cityRepository, never()).save(any());
  }

  @Test
  void should_fallbackToNominatimReverse_when_nearestCityTooFar() {
    CityEntity distantCity = buildCityEntity("tashkent", "Tashkent", 41.3, 69.24);
    given(cityRepository.findNearestCity(64.1466, -21.9426)).willReturn(distantCity);
    given(nominatimProperties.getMaxDistanceKm()).willReturn(50.0);

    NominatimPlace place =
        new NominatimPlace(
            "64.1466",
            "-21.9426",
            "Reykjavik",
            123456L,
            "relation",
            "city",
            new NominatimAddress("Reykjavik", null, null, "Iceland", "is"));
    given(nominatimClient.reverse(64.1466, -21.9426)).willReturn(Optional.of(place));

    given(cityRepository.findById("R123456")).willReturn(Optional.empty());
    given(timeZoneResolver.resolve(64.1466, -21.9426)).willReturn("Atlantic/Reykjavik");

    CountryEntity iceland =
        CountryEntity.builder()
            .code("IS")
            .name("Iceland")
            .defaultMethod(CalculationMethod.MWL)
            .defaultMadhab(Madhab.HANAFI)
            .build();
    given(countryRepository.findById("IS")).willReturn(Optional.of(iceland));

    CityEntity savedCity =
        CityEntity.builder()
            .id("R123456")
            .name("Reykjavik")
            .country(iceland)
            .latitude(64.1466)
            .longitude(-21.9426)
            .timezone("Atlantic/Reykjavik")
            .build();
    given(cityRepository.save(any(CityEntity.class))).willReturn(savedCity);

    CityResponse cityResponse =
        CityResponse.builder()
            .id("R123456")
            .name("Reykjavik")
            .country("IS")
            .latitude(64.1466)
            .longitude(-21.9426)
            .timezone("Atlantic/Reykjavik")
            .defaultMethod("MWL")
            .defaultMadhab("HANAFI")
            .build();
    given(cityMapper.toCityResponse(savedCity)).willReturn(cityResponse);

    NearestCityResponse result = cityService.findNearestCity(64.1466, -21.9426);

    assertThat(result.getCity().getId()).isEqualTo("R123456");
    assertThat(result.getCity().getTimezone()).isEqualTo("Atlantic/Reykjavik");
  }

  @Test
  void should_returnExistingCity_when_getOrCreateCityFindsInDb() {
    CityEntity city = buildCityEntity("tashkent", "Tashkent", 41.2995, 69.2401);
    given(cityRepository.findById("tashkent")).willReturn(Optional.of(city));

    CityEntity result = cityService.getOrCreateCity("tashkent");

    assertThat(result.getName()).isEqualTo("Tashkent");
    verify(nominatimClient, never()).lookup(any());
  }

  @Test
  void should_createCity_when_getOrCreateCityWithOsmId() {
    given(cityRepository.findById("R123456")).willReturn(Optional.empty());

    NominatimPlace place =
        new NominatimPlace(
            "64.1466",
            "-21.9426",
            "Reykjavik",
            123456L,
            "relation",
            "city",
            new NominatimAddress("Reykjavik", null, null, "Iceland", "is"));
    given(nominatimClient.lookup("R123456")).willReturn(Optional.of(place));

    // findById inside createCityFromNominatim
    given(cityRepository.findById("R123456"))
        .willReturn(Optional.empty())
        .willReturn(Optional.empty());
    given(timeZoneResolver.resolve(64.1466, -21.9426)).willReturn("Atlantic/Reykjavik");

    CountryEntity iceland =
        CountryEntity.builder()
            .code("IS")
            .name("Iceland")
            .defaultMethod(CalculationMethod.MWL)
            .defaultMadhab(Madhab.HANAFI)
            .build();
    given(countryRepository.findById("IS")).willReturn(Optional.of(iceland));

    CityEntity savedCity =
        CityEntity.builder()
            .id("R123456")
            .name("Reykjavik")
            .country(iceland)
            .latitude(64.1466)
            .longitude(-21.9426)
            .timezone("Atlantic/Reykjavik")
            .build();
    given(cityRepository.save(any(CityEntity.class))).willReturn(savedCity);

    CityEntity result = cityService.getOrCreateCity("R123456");

    assertThat(result.getName()).isEqualTo("Reykjavik");
    verify(cityRepository).save(any(CityEntity.class));
  }

  @Test
  void should_throwCityNotFound_when_getOrCreateCityWithUnknownId() {
    given(cityRepository.findById("unknown")).willReturn(Optional.empty());
    given(nominatimClient.lookup("unknown")).willReturn(Optional.empty());

    assertThatThrownBy(() -> cityService.getOrCreateCity("unknown"))
        .isInstanceOf(CityNotFoundException.class)
        .hasMessageContaining("unknown");
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
