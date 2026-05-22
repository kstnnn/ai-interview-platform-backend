package io.github.kstnnn.ai.interview.service.repository;

import io.github.kstnnn.ai.interview.service.model.SessionTopicState;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

public interface SessionTopicStateRepository extends ListCrudRepository<SessionTopicState, Long> {

  Optional<SessionTopicState> findBySessionIdAndTopic(UUID sessionId, String topic);

  List<SessionTopicState> findAllBySessionId(UUID sessionId);
}
