package uz.myrafeeq.api.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uz.myrafeeq.api.TestDataFactory.aCity;
import static uz.myrafeeq.api.TestDataFactory.aCountry;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uz.myrafeeq.api.dto.request.BulkCreateCitiesRequest;
import uz.myrafeeq.api.dto.request.CreateCityRequest;
import uz.myrafeeq.api.dto.request.UpdateCityRequest;
import uz.myrafeeq.api.dto.response.AdminCityResponse;
import uz.myrafeeq.api.entity.CityEntity;
import uz.myrafeeq.api.entity.CountryEntity;
import uz.myrafeeq.api.exception.CityNotFoundException;
import uz.myrafeeq.api.exception.CountryNotFoundException;
import uz.myrafeeq.api.exception.RequestValidationException;
import uz.myrafeeq.api.mapper.AdminCityMapper;
import uz.myrafeeq.api.repository.CityRepository;
import uz.myrafeeq.api.repository.CountryRepository;
import uz.myrafeeq.api.repository.UserPreferencesRepository;

@ExtendWith(MockitoExtension.class)
class AdminCityServiceTest {

  @Mock private CityRepository cityRepository;
  @Mock private CountryRepository countryRepository;
  @Mock private UserPreferencesRepository userPreferencesRepository;
  @Mock private AdminCityMapper adminCityMapper;
  @InjectMocks private AdminCityService adminCityService;

  @Test
  void should_listCities_when_noCountryFilter() {
    CityEntity city = aCity().build();
    AdminCityResponse response = AdminCityResponse.builder().id("tashkent").build();

    given(cityRepository.findAllWithCountry(any(Pageable.class)))
        .willReturn(new PageImpl<>(List.of(city)));
    given(adminCityMapper.toAdminCityResponse(city)).willReturn(response);

    Page<AdminCityResponse> result = adminCityService.listCities(null, 0, 20);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getId()).isEqualTo("tashkent");
  }

  @Test
  void should_listCities_when_filteredByCountry() {
    CityEntity city = aCity().build();
    AdminCityResponse response = AdminCityResponse.builder().id("tashkent").build();

    given(cityRepository.findByCountryCode(any(), any(Pageable.class)))
        .willReturn(new PageImpl<>(List.of(city)));
    given(adminCityMapper.toAdminCityResponse(city)).willReturn(response);

    Page<AdminCityResponse> result = adminCityService.listCities("UZ", 0, 20);

    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  void should_createCity_when_validRequest() {
    CountryEntity country = aCountry().build();
    CreateCityRequest request =
        new CreateCityRequest("bukhara", "Bukhara", "UZ", 39.77, 64.42, "Asia/Tashkent");
    AdminCityResponse response = AdminCityResponse.builder().id("bukhara").build();

    given(cityRepository.existsById("bukhara")).willReturn(false);
    given(countryRepository.findById("UZ")).willReturn(Optional.of(country));
    given(cityRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
    given(adminCityMapper.toAdminCityResponse(any(CityEntity.class))).willReturn(response);

    AdminCityResponse result = adminCityService.createCity(request);

    assertThat(result.getId()).isEqualTo("bukhara");
  }

  @Test
  void should_throwValidation_when_duplicateCityId() {
    CreateCityRequest request =
        new CreateCityRequest("tashkent", "Tashkent", "UZ", 41.3, 69.2, "Asia/Tashkent");

    given(cityRepository.existsById("tashkent")).willReturn(true);

    assertThatThrownBy(() -> adminCityService.createCity(request))
        .isInstanceOf(RequestValidationException.class)
        .hasMessageContaining("already exists");
  }

  @Test
  void should_throwCountryNotFound_when_invalidCountryCode() {
    CreateCityRequest request = new CreateCityRequest("city1", "City", "XX", 0.0, 0.0, "UTC");

    given(cityRepository.existsById("city1")).willReturn(false);
    given(countryRepository.findById("XX")).willReturn(Optional.empty());

    assertThatThrownBy(() -> adminCityService.createCity(request))
        .isInstanceOf(CountryNotFoundException.class);
  }

  @Test
  void should_updateCity_when_exists() {
    CityEntity city = aCity().build();
    UpdateCityRequest request =
        new UpdateCityRequest("Tashkent Updated", 41.3, 69.3, "Asia/Tashkent");
    AdminCityResponse response =
        AdminCityResponse.builder().id("tashkent").name("Tashkent Updated").build();

    given(cityRepository.findById("tashkent")).willReturn(Optional.of(city));
    given(cityRepository.save(any())).willReturn(city);
    given(adminCityMapper.toAdminCityResponse(any(CityEntity.class))).willReturn(response);

    AdminCityResponse result = adminCityService.updateCity("tashkent", request);

    assertThat(result.getName()).isEqualTo("Tashkent Updated");
  }

  @Test
  void should_throwCityNotFound_when_updateNonExisting() {
    UpdateCityRequest request = new UpdateCityRequest("City", 0.0, 0.0, "UTC");

    given(cityRepository.findById("nonexistent")).willReturn(Optional.empty());

    assertThatThrownBy(() -> adminCityService.updateCity("nonexistent", request))
        .isInstanceOf(CityNotFoundException.class);
  }

  @Test
  void should_deleteCity_when_noReferences() {
    CityEntity city = aCity().build();

    given(cityRepository.findById("tashkent")).willReturn(Optional.of(city));
    given(userPreferencesRepository.existsByCityId("tashkent")).willReturn(false);

    adminCityService.deleteCity("tashkent");

    verify(cityRepository).delete(city);
  }

  @Test
  void should_throwValidation_when_deleteCityWithReferences() {
    CityEntity city = aCity().build();

    given(cityRepository.findById("tashkent")).willReturn(Optional.of(city));
    given(userPreferencesRepository.existsByCityId("tashkent")).willReturn(true);

    assertThatThrownBy(() -> adminCityService.deleteCity("tashkent"))
        .isInstanceOf(RequestValidationException.class)
        .hasMessageContaining("user preferences reference it");
  }

  @Test
  void should_bulkCreateCities_when_validRequest() {
    CountryEntity country = aCountry().build();
    CreateCityRequest req1 =
        new CreateCityRequest("city1", "City1", "UZ", 40.0, 65.0, "Asia/Tashkent");
    CreateCityRequest req2 =
        new CreateCityRequest("city2", "City2", "UZ", 39.0, 66.0, "Asia/Tashkent");
    BulkCreateCitiesRequest request = new BulkCreateCitiesRequest(List.of(req1, req2));

    given(countryRepository.findAllById(any())).willReturn(List.of(country));
    given(cityRepository.saveAll(any())).willAnswer(inv -> inv.getArgument(0));
    given(adminCityMapper.toAdminCityResponse(any(CityEntity.class)))
        .willReturn(AdminCityResponse.builder().build());

    var result = adminCityService.bulkCreateCities(request);

    assertThat(result.getCreated()).isEqualTo(2);
  }

  @Test
  void should_throwValidation_when_bulkCreateWithMissingCountry() {
    CreateCityRequest req = new CreateCityRequest("city1", "City1", "XX", 40.0, 65.0, "UTC");
    BulkCreateCitiesRequest request = new BulkCreateCitiesRequest(List.of(req));

    given(countryRepository.findAllById(any())).willReturn(List.of());

    assertThatThrownBy(() -> adminCityService.bulkCreateCities(request))
        .isInstanceOf(RequestValidationException.class)
        .hasMessageContaining("Countries not found");
  }
}
