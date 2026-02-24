package uz.myrafeeq.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.myrafeeq.api.enums.CalculationMethod;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cities")
public class CityEntity {

  @Id private String id;

  @Column(nullable = false)
  private String nameEn;

  @Column(nullable = false)
  private String nameAr;

  @Column(nullable = false)
  private String nameUz;

  @Column(nullable = false)
  private String nameRu;

  @Column(nullable = false, length = 3)
  private String countryCode;

  @Column(nullable = false)
  private Double latitude;

  @Column(nullable = false)
  private Double longitude;

  @Column(nullable = false)
  private String timezone;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private CalculationMethod recommendedMethod;

  private Integer population;
}
