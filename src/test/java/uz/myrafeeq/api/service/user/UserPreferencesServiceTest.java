package uz.myrafeeq.api.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.myrafeeq.api.dto.request.UpdatePreferencesRequest;
import uz.myrafeeq.api.dto.response.CityResponse;
import uz.myrafeeq.api.dto.response.UserPreferencesResponse;
import uz.myrafeeq.api.entity.CityEntity;
import uz.myrafeeq.api.entity.CountryEntity;
import uz.myrafeeq.api.entity.UserPreferencesEntity;
import uz.myrafeeq.api.enums.CalculationMethod;
import uz.myrafeeq.api.enums.Madhab;
import uz.myrafeeq.api.exception.CityNotFoundException;
import uz.myrafeeq.api.exception.PreferencesNotFoundException;
import uz.myrafeeq.api.mapper.CityMapper;
import uz.myrafeeq.api.mapper.PreferencesMapper;
import uz.myrafeeq.api.repository.CityRepository;
import uz.myrafeeq.api.repository.UserPreferencesRepository;

@ExtendWith(MockitoExtension.class)
class UserPreferencesServiceTest {

  private static final Long TELEGRAM_ID = 123456789L;

  @Mock private UserPreferencesRepository preferencesRepository;
  @Mock private CityRepository cityRepository;
  @Mock private PreferencesMapper preferencesMapper;
  @Mock private CityMapper cityMapper;
  @InjectMocks private UserPreferencesService preferencesService;

  @Test
  void should_returnPreferences_when_userExists() {
    UserPreferencesEntity prefs = buildPreferencesEntity();
    CityEntity cityEntity = buildCityEntity();
    CityResponse cityResponse = buildCityResponse();
    UserPreferencesResponse expectedResponse = buildPreferencesResponse();

    given(preferencesRepository.findById(TELEGRAM_ID)).willReturn(Optional.of(prefs));
    given(cityRepository.findById("tashkent")).willReturn(Optional.of(cityEntity));
    given(cityMapper.toCityResponse(cityEntity)).willReturn(cityResponse);
    given(preferencesMapper.toPreferencesResponse(prefs, cityResponse))
        .willReturn(expectedResponse);

    UserPreferencesResponse result = preferencesService.getPreferences(TELEGRAM_ID);

    assertThat(result.calculationMethod()).isEqualTo("MBOUZ");
    assertThat(result.madhab()).isEqualTo("HANAFI");
  }

  @Test
  void should_throwPreferencesNotFound_when_noPreferences() {
    given(preferencesRepository.findById(TELEGRAM_ID)).willReturn(Optional.empty());

    assertThatThrownBy(() -> preferencesService.getPreferences(TELEGRAM_ID))
        .isInstanceOf(PreferencesNotFoundException.class)
        .hasMessageContaining(TELEGRAM_ID.toString());
  }

  @Test
  void should_updatePreferences_when_validRequest() {
    UserPreferencesEntity prefs = buildPreferencesEntity();
    CityEntity cityEntity = buildCityEntity();
    CityResponse cityResponse = buildCityResponse();
    UserPreferencesResponse expectedResponse = buildPreferencesResponse();

    UpdatePreferencesRequest request =
        new UpdatePreferencesRequest(
            null, CalculationMethod.MWL, null, null, null, null, null, null, null, null, null);

    given(preferencesRepository.findById(TELEGRAM_ID)).willReturn(Optional.of(prefs));
    given(preferencesRepository.save(any())).willReturn(prefs);
    given(cityRepository.findById("tashkent")).willReturn(Optional.of(cityEntity));
    given(cityMapper.toCityResponse(cityEntity)).willReturn(cityResponse);
    given(preferencesMapper.toPreferencesResponse(prefs, cityResponse))
        .willReturn(expectedResponse);

    UserPreferencesResponse result = preferencesService.updatePreferences(TELEGRAM_ID, request);

    assertThat(result).isNotNull();
  }

  @Test
  void should_throwPreferencesNotFound_when_updateNonExisting() {
    UpdatePreferencesRequest request =
        new UpdatePreferencesRequest(
            null, null, null, null, null, null, null, null, null, null, null);

    given(preferencesRepository.findById(TELEGRAM_ID)).willReturn(Optional.empty());

    assertThatThrownBy(() -> preferencesService.updatePreferences(TELEGRAM_ID, request))
        .isInstanceOf(PreferencesNotFoundException.class);
  }

  @Test
  void should_throwCityNotFound_when_updateWithInvalidCity() {
    UserPreferencesEntity prefs = buildPreferencesEntity();
    UpdatePreferencesRequest request =
        new UpdatePreferencesRequest(
            "nonexistent", null, null, null, null, null, null, null, null, null, null);

    given(preferencesRepository.findById(TELEGRAM_ID)).willReturn(Optional.of(prefs));
    given(cityRepository.findById("nonexistent")).willReturn(Optional.empty());

    assertThatThrownBy(() -> preferencesService.updatePreferences(TELEGRAM_ID, request))
        .isInstanceOf(CityNotFoundException.class)
        .hasMessageContaining("nonexistent");
  }

  private UserPreferencesEntity buildPreferencesEntity() {
    return UserPreferencesEntity.builder()
        .telegramId(TELEGRAM_ID)
        .cityId("tashkent")
        .latitude(41.2995)
        .longitude(69.2401)
        .calculationMethod(CalculationMethod.MBOUZ)
        .madhab(Madhab.HANAFI)
        .build();
  }

  private CityEntity buildCityEntity() {
    CountryEntity country =
        CountryEntity.builder()
            .code("UZ")
            .name("Uzbekistan")
            .defaultMethod(CalculationMethod.MBOUZ)
            .defaultMadhab(Madhab.HANAFI)
            .build();
    return CityEntity.builder()
        .id("tashkent")
        .name("Tashkent")
        .country(country)
        .latitude(41.2995)
        .longitude(69.2401)
        .timezone("Asia/Tashkent")
        .build();
  }

  private CityResponse buildCityResponse() {
    return CityResponse.builder()
        .id("tashkent")
        .name("Tashkent")
        .country("UZ")
        .latitude(41.2995)
        .longitude(69.2401)
        .timezone("Asia/Tashkent")
        .defaultMethod("MBOUZ")
        .defaultMadhab("HANAFI")
        .build();
  }

  private UserPreferencesResponse buildPreferencesResponse() {
    return UserPreferencesResponse.builder()
        .calculationMethod("MBOUZ")
        .madhab("HANAFI")
        .hijriCorrection(0)
        .build();
  }
}
