package uz.myrafeeq.api.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.myrafeeq.api.dto.response.AdminStatsResponse;
import uz.myrafeeq.api.repository.CityRepository;
import uz.myrafeeq.api.repository.CountryRepository;
import uz.myrafeeq.api.repository.PrayerTrackingRepository;
import uz.myrafeeq.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminStatsServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private CountryRepository countryRepository;
  @Mock private CityRepository cityRepository;
  @Mock private PrayerTrackingRepository prayerTrackingRepository;
  @InjectMocks private AdminStatsService adminStatsService;

  @Test
  void should_returnStats_when_dataExists() {
    given(userRepository.count()).willReturn(100L);
    given(userRepository.countByOnboardingCompletedTrue()).willReturn(75L);
    given(countryRepository.count()).willReturn(5L);
    given(cityRepository.count()).willReturn(50L);
    given(prayerTrackingRepository.count()).willReturn(10000L);

    AdminStatsResponse result = adminStatsService.getStats();

    assertThat(result.getTotalUsers()).isEqualTo(100L);
    assertThat(result.getOnboardedUsers()).isEqualTo(75L);
    assertThat(result.getTotalCountries()).isEqualTo(5L);
    assertThat(result.getTotalCities()).isEqualTo(50L);
    assertThat(result.getTotalTrackingRecords()).isEqualTo(10000L);
  }

  @Test
  void should_returnZeros_when_emptyDatabase() {
    given(userRepository.count()).willReturn(0L);
    given(userRepository.countByOnboardingCompletedTrue()).willReturn(0L);
    given(countryRepository.count()).willReturn(0L);
    given(cityRepository.count()).willReturn(0L);
    given(prayerTrackingRepository.count()).willReturn(0L);

    AdminStatsResponse result = adminStatsService.getStats();

    assertThat(result.getTotalUsers()).isZero();
    assertThat(result.getOnboardedUsers()).isZero();
  }
}
