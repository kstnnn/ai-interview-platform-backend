package io.github.kstnnn.ai.interview.service.repository;

import io.github.kstnnn.ai.interview.service.model.SessionQuestion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

public interface SessionQuestionRepository extends ListCrudRepository<SessionQuestion, UUID> {

  List<SessionQuestion> findBySessionIdAndParentQuestionIsNullOrderByRoundNumberAsc(UUID sessionId);

  List<SessionQuestion> findByParentQuestionId(UUID parentQuestionId);

  Optional<SessionQuestion> findBySessionIdAndRoundNumber(UUID sessionId, int roundNumber);

  long countByParentQuestionId(UUID parentQuestionId);
}
