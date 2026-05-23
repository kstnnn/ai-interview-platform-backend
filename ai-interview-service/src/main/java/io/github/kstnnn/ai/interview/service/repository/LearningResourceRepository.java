package io.github.kstnnn.ai.interview.service.repository;

import io.github.kstnnn.ai.interview.service.model.LearningResource;
import java.util.List;
import org.springframework.data.repository.ListCrudRepository;

public interface LearningResourceRepository extends ListCrudRepository<LearningResource, Long> {

  List<LearningResource> findByActiveTrueOrderByTopicAscTitleAsc();
}
