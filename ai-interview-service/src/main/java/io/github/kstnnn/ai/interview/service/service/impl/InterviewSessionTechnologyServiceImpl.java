package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.model.InterviewSession;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionTechnology;
import io.github.kstnnn.ai.interview.service.model.Technology;
import io.github.kstnnn.ai.interview.service.repository.InterviewSessionTechnologyRepository;
import io.github.kstnnn.ai.interview.service.repository.TechnologyRepository;
import io.github.kstnnn.ai.interview.service.service.InterviewSessionTechnologyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterviewSessionTechnologyServiceImpl implements InterviewSessionTechnologyService {

  private final TechnologyRepository tRepository;
  private final InterviewSessionTechnologyRepository iRepository;

  @Override
  public void saveSessionTechnologies(List<Technology> technologies, InterviewSession session) {
    var technologyKeys = technologies.stream().map(Technology::getKey).toList();
    var technologyIds = tRepository.findIdsByKeys(technologyKeys);
    var sessionTechnologies =
        technologyIds.stream()
            .map(id -> InterviewSessionTechnology.builder().id(id).session(session).build())
            .toList();
    iRepository.saveAll(sessionTechnologies);
  }
}
