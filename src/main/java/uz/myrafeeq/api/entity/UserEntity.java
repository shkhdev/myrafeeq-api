package uz.myrafeeq.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

  @Id
  @Column(nullable = false)
  private Long telegramId;

  @Column(nullable = false, length = 64)
  private String firstName;

  @Column(length = 32)
  private String username;

  @Column(length = 10)
  private String languageCode;

  @Builder.Default
  @Column(nullable = false)
  private Boolean onboardingCompleted = false;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private Instant updatedAt;

  @Version
  @Column(nullable = false)
  private Integer version;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserEntity that)) return false;
    return telegramId != null && telegramId.equals(that.telegramId);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
