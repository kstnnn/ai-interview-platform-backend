package io.github.kstnnn.ai.interview.service.repository;

import io.github.kstnnn.ai.interview.service.model.AnswerEvaluation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

public interface AnswerEvaluationRepository extends ListCrudRepository<AnswerEvaluation, UUID> {

  Optional<AnswerEvaluation> findBySessionAnswerId(UUID sessionAnswerId);
}
