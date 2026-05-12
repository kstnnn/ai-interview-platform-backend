package io.github.kstnnn.ai.interview.service.repository;

import io.github.kstnnn.ai.interview.service.model.Technology;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface TechnologyRepository extends ListCrudRepository<Technology, Long> {
  Optional<Technology> findByKey(String key);

  @Query("SELECT t.id FROM Technology t WHERE t.key IN :keys")
  List<Long> findIdsByKeys(List<String> keys);
}
