package io.github.kstnnn.ai.interview.service.repository;

import io.github.kstnnn.ai.interview.service.model.LearningResourceTag;
import java.util.List;
import org.springframework.data.repository.ListCrudRepository;

public interface LearningResourceTagRepository extends ListCrudRepository<LearningResourceTag, Long> {

  List<LearningResourceTag> findByResourceActiveTrue();
}
