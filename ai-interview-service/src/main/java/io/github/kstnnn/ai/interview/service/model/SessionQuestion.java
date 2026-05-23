package io.github.kstnnn.ai.interview.service.model;

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
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "session_questions")
@Getter
@Setter
@NoArgsConstructor
public class SessionQuestion {
  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "session_id", nullable = false)
  private InterviewSession session;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id")
  private Question question;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_question_id")
  private SessionQuestion parentQuestion;

  @Column(name = "round_number", nullable = false)
  private Integer roundNumber;

  @Column(nullable = false, length = 80)
  private String topic;

  @Column(length = 80)
  private String subtopic;

  @Column(nullable = false, length = 24)
  private Difficulty difficulty;

  @Column(name = "question_text_snapshot", nullable = false, columnDefinition = "text")
  private String questionTextSnapshot;

  @Column(name = "expected_answer_snapshot", columnDefinition = "text")
  private String expectedAnswerSnapshot;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, length = 32)
  private QuestionSourceType sourceType;

  @Column(name = "external_question_id")
  private UUID externalQuestionId;

  @Column(name = "selection_reason", length = 64)
  private SelectionReason selectionReason;

  @Column(name = "question_type", nullable = false, length = 24)
  private QuestionType questionType;

  @Column(name = "asked_at", nullable = false)
  private Instant askedAt;

  @PrePersist
  void onCreate() {
    if (askedAt == null) {
      askedAt = Instant.now();
    }
    if (questionType == null) {
      questionType = QuestionType.PRIMARY;
    }
    if (sourceType == null) {
      sourceType = QuestionSourceType.QUESTION_BANK;
    }
  }
}
