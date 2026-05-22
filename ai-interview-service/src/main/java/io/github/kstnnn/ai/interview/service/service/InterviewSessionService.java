package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.dto.StartInterviewSessionDto;
import java.util.UUID;

public interface InterviewSessionService {
  UUID initSession(StartInterviewSessionDto sessionDto);

  void cancelSession(UUID sessionId);
}
