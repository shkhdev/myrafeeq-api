package uz.myrafeeq.api.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.myrafeeq.api.entity.CityEntity;

public interface CityRepository extends JpaRepository<CityEntity, String> {

  @Query(
      """
      SELECT c FROM CityEntity c
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
