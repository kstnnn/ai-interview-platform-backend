package io.github.kstnnn.ai.interview.service.repository;

import io.github.kstnnn.ai.interview.service.model.Question;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
  List<Question> findAllByExternalIdIn(List<String> externalIds);

  @Query("SELECT q FROM Question q JOIN FETCH q.technology")
  List<Question> findAllWithTechnology();
}
