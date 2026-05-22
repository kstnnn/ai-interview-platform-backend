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
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "session_answers")
@Getter
@Setter
@NoArgsConstructor
public class SessionAnswer {
  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "session_question_id", nullable = false, unique = true)
  private SessionQuestion sessionQuestion;

  @Column(name = "answer_text", nullable = false, columnDefinition = "text")
  private String answerText;

  @Column(name = "answered_at", nullable = false)
  private Instant answeredAt;

  @Column(name = "duration_sec")
  private Integer durationSec;

  @PrePersist
  void onCreate() {
    if (answeredAt == null) {
      answeredAt = Instant.now();
    }
  }
}
