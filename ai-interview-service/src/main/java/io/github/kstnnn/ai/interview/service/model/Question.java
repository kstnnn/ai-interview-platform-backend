package io.github.kstnnn.ai.interview.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
public class Question {
  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "technology_id", nullable = false)
  private Technology technology;

  @Column(nullable = false, length = 80)
  private String topic;

  @Column(length = 80)
  private String subtopic;

  @Column(nullable = false, length = 24)
  private Difficulty difficulty;

  @Column(name = "question_text", nullable = false, columnDefinition = "text")
  private String questionText;

  @Column(name = "expected_answer", nullable = false, columnDefinition = "text")
  private String expectedAnswer;

  @Column(nullable = false)
  private boolean active = true;

  @Column(nullable = false)
  private Integer version = 1;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }
}
