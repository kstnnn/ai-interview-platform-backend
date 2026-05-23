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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import io.github.kstnnn.ai.interview.service.model.Difficulty;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Table(name = "planned_session_questions")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class PlannedSessionQuestion {

  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "session_id", nullable = false)
  private InterviewSession interviewSession;

  @OneToOne
  @JoinColumn(name = "question_id")
  private Question question;

  @Column(name = "question_text_snapshot", columnDefinition = "text")
  private String questionTextSnapshot;

  @Column(name = "expected_answer_snapshot", columnDefinition = "text")
  private String expectedAnswerSnapshot;

  @Column(name = "evaluation_rubric", columnDefinition = "text")
  private String evaluationRubric;

  @Column(length = 80)
  private String topic;

  @Column(length = 80)
  private String subtopic;

  @Column(length = 24)
  private Difficulty difficulty;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, length = 32)
  private QuestionSourceType sourceType;

  @Column(name = "external_question_id")
  private UUID externalQuestionId;

  @Column(name = "display_order")
  private Integer displayOrder;

  @Column(name = "planned_status", nullable = false)
  private PlannedStatus plannedStatus;
}
