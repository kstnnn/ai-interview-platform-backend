package io.github.kstnnn.ai.interview.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "learning_resources")
@Getter
@Setter
@NoArgsConstructor
public class LearningResource {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 80)
  private String topic;

  @Column(nullable = false, length = 240)
  private String title;

  @Column(nullable = false, length = 1024)
  private String url;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private LearningResourceType type;

  @Column(nullable = false, length = 8)
  private String language;

  @Column(nullable = false, length = 24)
  private String difficulty;

  @Column(nullable = false)
  private boolean active = true;
}
