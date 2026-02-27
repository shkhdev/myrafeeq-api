package uz.myrafeeq.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import uz.myrafeeq.api.entity.CityEntity;
import uz.myrafeeq.api.entity.CountryEntity;
import uz.myrafeeq.api.enums.CalculationMethod;
import uz.myrafeeq.api.enums.Madhab;

class CityRepositoryTest extends RepositoryTest {

  @Autowired private CityRepository cityRepository;
  @Autowired private CountryRepository countryRepository;

  private CountryEntity country;

  @BeforeEach
  void setUp() {
    cityRepository.deleteAll();
    countryRepository.deleteAll();

    country =
        countryRepository.save(
            CountryEntity.builder()
                .code("UZ")
                .name("Uzbekistan")
                .defaultMethod(CalculationMethod.MBOUZ)
                .defaultMadhab(Madhab.HANAFI)
                .build());

    cityRepository.save(
        CityEntity.builder()
            .id("tashkent")
            .name("Tashkent")
            .country(country)
            .latitude(41.2995)
            .longitude(69.2401)
            .timezone("Asia/Tashkent")
            .build());

    cityRepository.save(
        CityEntity.builder()
            .id("samarkand")
            .name("Samarkand")
            .country(country)
            .latitude(39.6542)
            .longitude(66.9597)
            .timezone("Asia/Tashkent")
            .build());

    cityRepository.save(
        CityEntity.builder()
            .id("bukhara")
            .name("Bukhara")
            .country(country)
            .latitude(39.7681)
            .longitude(64.4219)
            .timezone("Asia/Tashkent")
            .build());
  }

  @Test
  void should_searchByName_when_partialMatch() {
    List<CityEntity> result = cityRepository.searchByName("tash", PageRequest.of(0, 10));

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getName()).isEqualTo("Tashkent");
  }

  @Test
  void should_searchByName_when_caseInsensitive() {
    List<CityEntity> result = cityRepository.searchByName("SAMARKAND", PageRequest.of(0, 10));

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getId()).isEqualTo("samarkand");
  }

  @Test
  void should_returnEmpty_when_noMatch() {
    List<CityEntity> result = cityRepository.searchByName("NonExistent", PageRequest.of(0, 10));

    assertThat(result).isEmpty();
  }

  @Test
  void should_respectLimit_when_searching() {
    List<CityEntity> result = cityRepository.searchByName("a", PageRequest.of(0, 2));

    assertThat(result).hasSize(2);
  }

  @Test
  void should_findNearestCity_when_haversineDistance() {
    // Point close to Tashkent
    CityEntity nearest = cityRepository.findNearestCity(41.3, 69.3);

    assertThat(nearest).isNotNull();
    assertThat(nearest.getId()).isEqualTo("tashkent");
  }

  @Test
  void should_findNearestCity_when_closerToSamarkand() {
    // Point closer to Samarkand
    CityEntity nearest = cityRepository.findNearestCity(39.7, 67.0);

    assertThat(nearest).isNotNull();
    assertThat(nearest.getId()).isEqualTo("samarkand");
  }

  @Test
  void should_findByCountryCode_when_paged() {
    Page<CityEntity> result = cityRepository.findByCountryCode("UZ", PageRequest.of(0, 10));

    assertThat(result.getContent()).hasSize(3);
    assertThat(result.getContent()).allMatch(c -> c.getCountry().getCode().equals("UZ"));
  }

  @Test
  void should_existsByCountryCode() {
    assertThat(cityRepository.existsByCountryCode("UZ")).isTrue();
    assertThat(cityRepository.existsByCountryCode("XX")).isFalse();
  }
}
