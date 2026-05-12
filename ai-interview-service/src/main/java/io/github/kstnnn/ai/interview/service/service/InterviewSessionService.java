package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.dto.StartInterviewSessionDto;

public interface InterviewSessionService {
  void startInterview(StartInterviewSessionDto sessionDto);
}
