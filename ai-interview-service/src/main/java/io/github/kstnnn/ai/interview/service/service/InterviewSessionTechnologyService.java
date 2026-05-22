package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.model.InterviewSession;
import java.util.List;

public interface InterviewSessionTechnologyService {

  void saveSessionTechnologies(List<String> technologyKeys, InterviewSession session);
}
