package uz.myrafeeq.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uz.myrafeeq.api.entity.PrayerTrackingEntity;
import uz.myrafeeq.api.entity.UserEntity;
import uz.myrafeeq.api.enums.PrayerName;
import uz.myrafeeq.api.repository.projection.DateCountProjection;
import uz.myrafeeq.api.repository.projection.PrayerCountProjection;

class PrayerTrackingRepositoryTest extends RepositoryTest {

  private static final Long TELEGRAM_ID = 123456789L;

  @Autowired private PrayerTrackingRepository trackingRepository;
  @Autowired private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    trackingRepository.deleteAll();
    userRepository.deleteAll();

    userRepository.save(
        UserEntity.builder()
            .telegramId(TELEGRAM_ID)
            .firstName("Doston")
            .languageCode("uz")
            .build());

    LocalDate today = LocalDate.now();
    Instant now = Instant.now();

    // Today: all 5 prayers completed
    for (PrayerName prayer : PrayerName.values()) {
      trackingRepository.save(
          PrayerTrackingEntity.builder()
              .telegramId(TELEGRAM_ID)
              .prayerDate(today)
              .prayerName(prayer)
              .prayed(true)
              .toggledAt(now)
              .build());
    }

    // Yesterday: 3 prayers completed
    trackingRepository.save(
        PrayerTrackingEntity.builder()
            .telegramId(TELEGRAM_ID)
            .prayerDate(today.minusDays(1))
            .prayerName(PrayerName.FAJR)
            .prayed(true)
            .toggledAt(now)
            .build());
    trackingRepository.save(
        PrayerTrackingEntity.builder()
            .telegramId(TELEGRAM_ID)
            .prayerDate(today.minusDays(1))
            .prayerName(PrayerName.DHUHR)
            .prayed(true)
            .toggledAt(now)
            .build());
    trackingRepository.save(
        PrayerTrackingEntity.builder()
            .telegramId(TELEGRAM_ID)
            .prayerDate(today.minusDays(1))
            .prayerName(PrayerName.MAGHRIB)
            .prayed(false)
            .toggledAt(now)
            .build());
  }

  @Test
  void should_findByTelegramIdAndPrayerDate() {
    List<PrayerTrackingEntity> result =
        trackingRepository.findByTelegramIdAndPrayerDate(TELEGRAM_ID, LocalDate.now());

    assertThat(result).hasSize(5);
    assertThat(result).allMatch(e -> e.getTelegramId().equals(TELEGRAM_ID));
  }

  @Test
  void should_findByTelegramIdAndPrayerDateBetween() {
    LocalDate from = LocalDate.now().minusDays(1);
    LocalDate to = LocalDate.now();

    List<PrayerTrackingEntity> result =
        trackingRepository.findByTelegramIdAndPrayerDateBetween(TELEGRAM_ID, from, to);

    assertThat(result).hasSize(8); // 5 today + 3 yesterday
  }

  @Test
  void should_findByTelegramIdAndPrayerDateAndPrayerName() {
    Optional<PrayerTrackingEntity> result =
        trackingRepository.findByTelegramIdAndPrayerDateAndPrayerName(
            TELEGRAM_ID, LocalDate.now(), PrayerName.FAJR);

    assertThat(result).isPresent();
    assertThat(result.get().getPrayed()).isTrue();
  }

  @Test
  void should_countCompletedByPrayer() {
    LocalDate from = LocalDate.now().minusDays(1);
    LocalDate to = LocalDate.now();

    List<PrayerCountProjection> result =
        trackingRepository.countCompletedByPrayer(TELEGRAM_ID, from, to);

    assertThat(result).isNotEmpty();
    // FAJR: 2 (today + yesterday), DHUHR: 2 (today + yesterday)
    Optional<PrayerCountProjection> fajr =
        result.stream().filter(p -> p.getPrayerName() == PrayerName.FAJR).findFirst();
    assertThat(fajr).isPresent();
    assertThat(fajr.get().getCount()).isEqualTo(2L);
  }

  @Test
  void should_countCompletedByDate() {
    LocalDate from = LocalDate.now().minusDays(7);
    LocalDate to = LocalDate.now();

    List<DateCountProjection> result =
        trackingRepository.countCompletedByDate(TELEGRAM_ID, from, to);

    assertThat(result).isNotEmpty();
    // Today: 5 completed, yesterday: 2 completed (prayed=true only)
    Optional<DateCountProjection> today =
        result.stream().filter(d -> d.getPrayerDate().equals(LocalDate.now())).findFirst();
    assertThat(today).isPresent();
    assertThat(today.get().getCount()).isEqualTo(5L);
  }

  @Test
  void should_returnEmpty_when_noDataForUser() {
    List<PrayerCountProjection> result =
        trackingRepository.countCompletedByPrayer(
            999999L, LocalDate.now().minusDays(7), LocalDate.now());

    assertThat(result).isEmpty();
  }
}
