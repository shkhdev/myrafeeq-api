package uz.myrafeeq.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.myrafeeq.api.entity.PrayerTrackingEntity;
import uz.myrafeeq.api.enums.PrayerName;

public interface PrayerTrackingRepository extends JpaRepository<PrayerTrackingEntity, UUID> {

  List<PrayerTrackingEntity> findByTelegramIdAndDate(Long telegramId, LocalDate date);

  List<PrayerTrackingEntity> findByTelegramIdAndDateBetween(
      Long telegramId, LocalDate from, LocalDate to);

  Optional<PrayerTrackingEntity> findByTelegramIdAndDateAndPrayerName(
      Long telegramId, LocalDate date, PrayerName prayerName);
}
