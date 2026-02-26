package uz.myrafeeq.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.myrafeeq.api.entity.UserPreferencesEntity;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferencesEntity, Long> {}
