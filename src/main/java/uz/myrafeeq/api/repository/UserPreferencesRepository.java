package uz.myrafeeq.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.myrafeeq.api.entity.UserPreferencesEntity;

public interface UserPreferencesRepository extends JpaRepository<UserPreferencesEntity, Long> {}
