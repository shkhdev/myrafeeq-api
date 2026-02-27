package uz.myrafeeq.api.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.myrafeeq.api.entity.CityEntity;

@Repository
public interface CityRepository extends JpaRepository<CityEntity, String> {

  boolean existsByCountryCode(String countryCode);

  @EntityGraph(attributePaths = "country")
  Page<CityEntity> findByCountryCode(String countryCode, Pageable pageable);

  @EntityGraph(attributePaths = "country")
  @Query("SELECT c FROM CityEntity c")
  Page<CityEntity> findAllWithCountry(Pageable pageable);

  @Override
  @EntityGraph(attributePaths = "country")
  Optional<CityEntity> findById(String id);

  @Query(
      """
      SELECT c FROM CityEntity c JOIN FETCH c.country
      WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))
      ORDER BY c.name
      """)
  List<CityEntity> searchByName(@Param("query") String query, Pageable pageable);

  @Query(
      value =
          """
          SELECT * FROM cities c
          ORDER BY (
            6371 * ACOS(
              COS(RADIANS(:lat)) * COS(RADIANS(c.latitude))
              * COS(RADIANS(c.longitude) - RADIANS(:lon))
              + SIN(RADIANS(:lat)) * SIN(RADIANS(c.latitude))
            )
          )
          LIMIT 1
          """,
      nativeQuery = true)
  CityEntity findNearestCity(@Param("lat") double lat, @Param("lon") double lon);
}
