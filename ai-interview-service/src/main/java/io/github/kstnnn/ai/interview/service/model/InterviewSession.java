package io.github.kstnnn.ai.interview.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "interview_sessions")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewSession {
  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "vacancy_id")
  private UUID vacancyId;

  @Column(name = "application_id")
  private UUID applicationId;

  @Enumerated(EnumType.STRING)
  @Column(name = "session_type", nullable = false, length = 32)
  private InterviewSessionType sessionType;

  @Column(name = "min_questions", nullable = false)
  private Integer minQuestions;

  @Column(name = "max_questions", nullable = false)
  private Integer maxQuestions;

  @Column(name = "min_questions_per_topic", nullable = false)
  private Integer minQuestionsPerTopic;

  @Column(name = "max_follow_ups_per_primary", nullable = false)
  private Integer maxFollowUpsPerPrimary;

  @Column(name = "target_confidence", nullable = false, precision = 4, scale = 3)
  private BigDecimal targetConfidence;

  @Column(nullable = false)
  private InterviewSessionStatus status;

  @Column(name = "finished_reason")
  private InterviewFinishedReason finishedReason;

  @Column(name = "interview_level")
  private InterviewLevel interviewLevel;

  @Column(name = "interview_language", nullable = false, length = 32)
  private String interviewLanguage;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "finished_at")
  private Instant finishedAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    if (status == null) {
      status = InterviewSessionStatus.CREATED;
    }
    if (sessionType == null) {
      sessionType = applicationId == null ? InterviewSessionType.MOCK : InterviewSessionType.VACANCY_APPLICATION;
    }
    if (minQuestionsPerTopic == null) {
      minQuestionsPerTopic = 2;
    }
    if (maxFollowUpsPerPrimary == null) {
      maxFollowUpsPerPrimary = 1;
    }
    if (interviewLanguage == null || interviewLanguage.isBlank()) {
      interviewLanguage = "Russian";
    }
  }
}
