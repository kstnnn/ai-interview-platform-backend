package io.github.kstnnn.organization.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "vacancies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vacancy {
  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @Column(nullable = false, length = 180)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Column(columnDefinition = "TEXT")
  private String requirements;

  @Column(length = 160)
  private String location;

  @Enumerated(EnumType.STRING)
  @Column(name = "employment_type", nullable = false, length = 32)
  private EmploymentType employmentType;

  @Enumerated(EnumType.STRING)
  @Column(name = "work_format", nullable = false, length = 32)
  private WorkFormat workFormat;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private VacancyLevel level;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private VacancyStatus status;

  @Column(name = "created_by_user_id", nullable = false)
  private UUID createdByUserId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void onCreate() {
    var now = Instant.now();
    if (createdAt == null) {
      createdAt = now;
    }
    if (updatedAt == null) {
      updatedAt = now;
    }
    if (status == null) {
      status = VacancyStatus.DRAFT;
    }
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }
}
