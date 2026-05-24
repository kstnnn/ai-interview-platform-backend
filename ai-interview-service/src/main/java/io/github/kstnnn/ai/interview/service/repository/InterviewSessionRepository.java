package io.github.kstnnn.ai.interview.service.repository;

import io.github.kstnnn.ai.interview.service.model.InterviewSession;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionStatus;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, UUID> {

  List<InterviewSession> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);

  List<InterviewSession> findTop10ByUserIdAndSessionTypeAndStatusOrderByFinishedAtDesc(
      UUID userId, InterviewSessionType sessionType, InterviewSessionStatus status);
}
