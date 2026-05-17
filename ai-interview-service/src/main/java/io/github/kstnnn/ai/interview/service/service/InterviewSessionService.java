package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.dto.StartInterviewSessionDto;
import java.util.UUID;

public interface InterviewSessionService {
  void initSession(StartInterviewSessionDto sessionDto);

  void startSession(UUID sessionId);

  void stopSession(UUID sessionId);
}
