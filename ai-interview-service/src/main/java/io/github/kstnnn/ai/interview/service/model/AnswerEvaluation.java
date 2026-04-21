package io.github.kstnnn.ai.interview.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "answer_evaluations")
@Getter
@Setter
@NoArgsConstructor
public class AnswerEvaluation {
  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "session_answer_id", nullable = false, unique = true)
  private SessionAnswer sessionAnswer;

  @Column(name = "correctness_score", nullable = false, precision = 3, scale = 2)
  private BigDecimal correctnessScore;

  @Column(name = "depth_score", nullable = false, precision = 3, scale = 2)
  private BigDecimal depthScore;

  @Column(name = "practical_score", nullable = false, precision = 3, scale = 2)
  private BigDecimal practicalScore;

  @Column(name = "total_score", nullable = false, precision = 3, scale = 2)
  private BigDecimal totalScore;

  @Column(columnDefinition = "text")
  private String feedback;

  @Column(name = "knowledge_gaps_json", columnDefinition = "jsonb")
  private String knowledgeGapsJson;

  @Column(name = "evaluated_at", nullable = false)
  private Instant evaluatedAt;

  @PrePersist
  void onCreate() {
    if (evaluatedAt == null) {
      evaluatedAt = Instant.now();
    }
  }
}
