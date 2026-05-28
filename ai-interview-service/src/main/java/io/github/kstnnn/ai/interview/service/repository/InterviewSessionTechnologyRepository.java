package io.github.kstnnn.ai.interview.service.repository;

import io.github.kstnnn.ai.interview.service.model.InterviewSessionTechnology;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.ListCrudRepository;

public interface InterviewSessionTechnologyRepository
    extends ListCrudRepository<InterviewSessionTechnology, Long> {

  @Query(
      """
      select ist.technology.key
      from InterviewSessionTechnology ist
      where ist.session.id = :sessionId
      order by ist.technology.key
      """)
  List<String> findTechnologyKeysBySessionId(@Param("sessionId") UUID sessionId);

  @Query(
      """
      select ist.technology.displayName
      from InterviewSessionTechnology ist
      where ist.session.id = :sessionId
      order by ist.technology.sortOrder, ist.technology.displayName
      """)
  List<String> findTechnologyDisplayNamesBySessionId(@Param("sessionId") UUID sessionId);
}
