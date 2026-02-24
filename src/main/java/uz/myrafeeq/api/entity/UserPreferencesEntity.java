package uz.myrafeeq.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.myrafeeq.api.enums.CalculationMethod;
import uz.myrafeeq.api.enums.HighLatitudeRule;
import uz.myrafeeq.api.enums.Madhab;
import uz.myrafeeq.api.enums.ReminderTiming;
import uz.myrafeeq.api.enums.ThemePreference;
import uz.myrafeeq.api.enums.TimeFormat;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_preferences")
@EntityListeners(AuditingEntityListener.class)
public class UserPreferencesEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private Long telegramId;

  private String cityId;

  private Double latitude;

  private Double longitude;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private CalculationMethod calculationMethod;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private Madhab madhab;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private HighLatitudeRule highLatitudeRule;

  @Builder.Default
  @Column(nullable = false)
  private Integer hijriCorrection = 0;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private TimeFormat timeFormat;

  @Enumerated(EnumType.STRING)
  @Column(length = 10)
  private ThemePreference theme;

  @Builder.Default
  @Column(nullable = false)
  private Boolean notificationsEnabled = true;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private ReminderTiming reminderTiming;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private String prayerNotifications;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private String manualAdjustments;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private Instant updatedAt;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserPreferencesEntity that)) return false;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
