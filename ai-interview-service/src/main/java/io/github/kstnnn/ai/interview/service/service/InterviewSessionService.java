package io.github.kstnnn.ai.interview.service.service;

import java.util.List;
import java.util.UUID;

public interface InterviewSessionService {
  void startInterview(UUID userId, List<String> technologies);

  List<String> getBaseQuestions(List<String> tech);
}
