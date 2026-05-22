package io.github.kstnnn.ai.interview.service.repository;

import io.github.kstnnn.ai.interview.service.model.SessionAnswer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

public interface SessionAnswerRepository extends ListCrudRepository<SessionAnswer, UUID> {

  Optional<SessionAnswer> findBySessionQuestionId(UUID sessionQuestionId);
}
