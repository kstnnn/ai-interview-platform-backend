package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.model.InterviewSession;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionTechnology;
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
  public void saveSessionTechnologies(List<String> technologyKeys, InterviewSession session) {
    var technologyIds = tRepository.findIdsByKeys(technologyKeys);
    List<InterviewSessionTechnology> sessionTechnologies =
        technologyIds.stream()
            .map(
                id -> {
                  var tech = tRepository.getReferenceById(id);
                  return new InterviewSessionTechnology(null, session, tech);
                })
            .toList();
    iRepository.saveAll(sessionTechnologies);
  }
}
