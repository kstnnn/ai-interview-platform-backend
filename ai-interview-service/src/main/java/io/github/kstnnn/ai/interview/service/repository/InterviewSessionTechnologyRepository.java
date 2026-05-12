package io.github.kstnnn.ai.interview.service.repository;

import io.github.kstnnn.ai.interview.service.model.InterviewSessionTechnology;
import org.springframework.data.repository.ListCrudRepository;

public interface InterviewSessionTechnologyRepository
    extends ListCrudRepository<InterviewSessionTechnology, Long> {}
