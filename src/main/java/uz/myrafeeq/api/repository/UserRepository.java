package uz.myrafeeq.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.myrafeeq.api.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {}
