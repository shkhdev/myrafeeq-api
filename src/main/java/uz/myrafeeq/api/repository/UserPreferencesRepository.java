package uz.myrafeeq.api.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.myrafeeq.api.entity.UserPreferencesEntity;

public interface UserPreferencesRepository extends JpaRepository<UserPreferencesEntity, UUID> {

  Optional<UserPreferencesEntity> findByTelegramId(Long telegramId);
}
