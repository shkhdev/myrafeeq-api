package uz.myrafeeq.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.myrafeeq.api.entity.PrayerTrackingEntity;
import uz.myrafeeq.api.enums.PrayerName;

public interface PrayerTrackingRepository extends JpaRepository<PrayerTrackingEntity, UUID> {

  List<PrayerTrackingEntity> findByTelegramIdAndPrayerDate(Long telegramId, LocalDate date);

  List<PrayerTrackingEntity> findByTelegramIdAndPrayerDateBetween(
      Long telegramId, LocalDate from, LocalDate to);

  Optional<PrayerTrackingEntity> findByTelegramIdAndPrayerDateAndPrayerName(
      Long telegramId, LocalDate date, PrayerName prayerName);

  @Query(
      """
      SELECT e.prayerName, COUNT(e) FROM PrayerTrackingEntity e
      WHERE e.telegramId = :telegramId AND e.prayerDate BETWEEN :from AND :to AND e.prayed = true
      GROUP BY e.prayerName
      """)
  List<Object[]> countCompletedByPrayer(
      @Param("telegramId") Long telegramId,
      @Param("from") LocalDate from,
      @Param("to") LocalDate to);

  @Query(
      """
      SELECT e.prayerDate, COUNT(e) FROM PrayerTrackingEntity e
      WHERE e.telegramId = :telegramId AND e.prayerDate BETWEEN :from AND :to AND e.prayed = true
      GROUP BY e.prayerDate
      ORDER BY e.prayerDate DESC
      """)
  List<Object[]> countCompletedByDate(
      @Param("telegramId") Long telegramId,
      @Param("from") LocalDate from,
      @Param("to") LocalDate to);
}
