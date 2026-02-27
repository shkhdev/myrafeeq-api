package uz.myrafeeq.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.myrafeeq.api.dto.response.AdminStatsResponse;
import uz.myrafeeq.api.repository.CityRepository;
import uz.myrafeeq.api.repository.CountryRepository;
import uz.myrafeeq.api.repository.PrayerTrackingRepository;
import uz.myrafeeq.api.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

  private final UserRepository userRepository;
  private final CountryRepository countryRepository;
  private final CityRepository cityRepository;
  private final PrayerTrackingRepository prayerTrackingRepository;

  @Transactional(readOnly = true)
  public AdminStatsResponse getStats() {
    return AdminStatsResponse.builder()
        .totalUsers(userRepository.count())
        .onboardedUsers(userRepository.countByOnboardingCompletedTrue())
        .totalCountries(countryRepository.count())
        .totalCities(cityRepository.count())
        .totalTrackingRecords(prayerTrackingRepository.count())
        .build();
  }
}
