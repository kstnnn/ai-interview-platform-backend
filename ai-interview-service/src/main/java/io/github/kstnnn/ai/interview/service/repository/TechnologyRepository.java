package io.github.kstnnn.ai.interview.service.repository;

import io.github.kstnnn.ai.interview.service.model.Technology;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TechnologyRepository extends JpaRepository<Technology, Long> {
  Optional<Technology> findByKey(String key);

  @Query("SELECT t.id FROM Technology t WHERE t.key IN :keys")
  List<Long> findIdsByKeys(List<String> keys);
}
