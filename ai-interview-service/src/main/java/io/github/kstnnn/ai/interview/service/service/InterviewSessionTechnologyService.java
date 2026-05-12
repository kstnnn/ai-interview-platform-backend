package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.model.InterviewSession;
import io.github.kstnnn.ai.interview.service.model.Technology;
import java.util.List;

public interface InterviewSessionTechnologyService {

  void saveSessionTechnologies(List<Technology> technologies, InterviewSession session);
}
