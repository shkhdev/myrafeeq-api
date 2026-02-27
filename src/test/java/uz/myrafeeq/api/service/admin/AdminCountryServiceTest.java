package uz.myrafeeq.api.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uz.myrafeeq.api.TestDataFactory.aCountry;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.myrafeeq.api.dto.request.CreateCountryRequest;
import uz.myrafeeq.api.dto.request.UpdateCountryRequest;
import uz.myrafeeq.api.dto.response.CountryResponse;
import uz.myrafeeq.api.entity.CountryEntity;
import uz.myrafeeq.api.enums.CalculationMethod;
import uz.myrafeeq.api.enums.Madhab;
import uz.myrafeeq.api.exception.CountryNotFoundException;
import uz.myrafeeq.api.exception.RequestValidationException;
import uz.myrafeeq.api.mapper.CountryMapper;
import uz.myrafeeq.api.repository.CityRepository;
import uz.myrafeeq.api.repository.CountryRepository;

@ExtendWith(MockitoExtension.class)
class AdminCountryServiceTest {

  @Mock private CountryRepository countryRepository;
  @Mock private CityRepository cityRepository;
  @Mock private CountryMapper countryMapper;
  @InjectMocks private AdminCountryService adminCountryService;

  @Test
  void should_listCountries() {
    CountryEntity country = aCountry().build();
    CountryResponse response = CountryResponse.builder().code("UZ").name("Uzbekistan").build();

    given(countryRepository.findAll()).willReturn(List.of(country));
    given(countryMapper.toCountryResponse(country)).willReturn(response);

    List<CountryResponse> result = adminCountryService.listCountries();

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getCode()).isEqualTo("UZ");
  }

  @Test
  void should_createCountry_when_valid() {
    CreateCountryRequest request =
        new CreateCountryRequest("TR", "Turkey", CalculationMethod.EGYPT, Madhab.HANAFI);
    CountryResponse response = CountryResponse.builder().code("TR").build();

    given(countryRepository.existsById("TR")).willReturn(false);
    given(countryRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
    given(countryMapper.toCountryResponse(any(CountryEntity.class))).willReturn(response);

    CountryResponse result = adminCountryService.createCountry(request);

    assertThat(result.getCode()).isEqualTo("TR");
  }

  @Test
  void should_throwValidation_when_duplicateCountryCode() {
    CreateCountryRequest request =
        new CreateCountryRequest("UZ", "Uzbekistan", CalculationMethod.MBOUZ, Madhab.HANAFI);

    given(countryRepository.existsById("UZ")).willReturn(true);

    assertThatThrownBy(() -> adminCountryService.createCountry(request))
        .isInstanceOf(RequestValidationException.class)
        .hasMessageContaining("already exists");
  }

  @Test
  void should_updateCountry_when_exists() {
    CountryEntity country = aCountry().build();
    UpdateCountryRequest request =
        new UpdateCountryRequest("Uzbekistan Republic", CalculationMethod.MBOUZ, Madhab.HANAFI);
    CountryResponse response = CountryResponse.builder().code("UZ").build();

    given(countryRepository.findById("UZ")).willReturn(Optional.of(country));
    given(countryRepository.save(any())).willReturn(country);
    given(countryMapper.toCountryResponse(any(CountryEntity.class))).willReturn(response);

    CountryResponse result = adminCountryService.updateCountry("UZ", request);

    assertThat(result).isNotNull();
  }

  @Test
  void should_throwCountryNotFound_when_updateNonExisting() {
    UpdateCountryRequest request =
        new UpdateCountryRequest("Name", CalculationMethod.MWL, Madhab.SHAFI);

    given(countryRepository.findById("XX")).willReturn(Optional.empty());

    assertThatThrownBy(() -> adminCountryService.updateCountry("XX", request))
        .isInstanceOf(CountryNotFoundException.class);
  }

  @Test
  void should_deleteCountry_when_noReferences() {
    CountryEntity country = aCountry().build();

    given(countryRepository.findById("UZ")).willReturn(Optional.of(country));
    given(cityRepository.existsByCountryCode("UZ")).willReturn(false);

    adminCountryService.deleteCountry("UZ");

    verify(countryRepository).delete(country);
  }

  @Test
  void should_throwValidation_when_deleteCountryWithCities() {
    CountryEntity country = aCountry().build();

    given(countryRepository.findById("UZ")).willReturn(Optional.of(country));
    given(cityRepository.existsByCountryCode("UZ")).willReturn(true);

    assertThatThrownBy(() -> adminCountryService.deleteCountry("UZ"))
        .isInstanceOf(RequestValidationException.class)
        .hasMessageContaining("cities reference it");
  }
}
