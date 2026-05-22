package io.github.kstnnn.ai.interview.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "session_topic_states",
    uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "topic"}))
@Getter
@Setter
@NoArgsConstructor
public class SessionTopicState {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "session_id", nullable = false)
  private InterviewSession session;

  @Column(nullable = false, length = 80)
  private String topic;

  @Column(name = "questions_asked", nullable = false)
  private Integer questionsAsked = 0;

  @Column(name = "avg_score", nullable = false, precision = 3, scale = 2)
  private BigDecimal avgScore = BigDecimal.ZERO;

  @Column(name = "mastery_score", nullable = false, precision = 3, scale = 2)
  private BigDecimal masteryScore = BigDecimal.ZERO;

  @Column(name = "confidence_score", nullable = false, precision = 3, scale = 2)
  private BigDecimal confidenceScore = BigDecimal.ZERO;

  @Column(name = "last_asked_round")
  private Integer lastAskedRound;
}
