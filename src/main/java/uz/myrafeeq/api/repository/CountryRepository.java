package uz.myrafeeq.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.myrafeeq.api.entity.CountryEntity;

@Repository
public interface CountryRepository extends JpaRepository<CountryEntity, String> {}
