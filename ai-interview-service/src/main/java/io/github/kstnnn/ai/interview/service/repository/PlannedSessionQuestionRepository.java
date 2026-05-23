package io.github.kstnnn.ai.interview.service.repository;

import io.github.kstnnn.ai.interview.service.model.PlannedSessionQuestion;
import io.github.kstnnn.ai.interview.service.model.PlannedStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

public interface PlannedSessionQuestionRepository
    extends ListCrudRepository<PlannedSessionQuestion, UUID> {

  Optional<PlannedSessionQuestion> findFirstByInterviewSessionIdAndPlannedStatusOrderByDisplayOrderAsc(
      UUID sessionId, PlannedStatus status);
}
