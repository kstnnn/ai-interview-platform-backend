package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.dto.InterviewHistoryDto;
import io.github.kstnnn.ai.interview.service.dto.InterviewSessionSummaryDto;
import io.github.kstnnn.ai.interview.service.dto.StartInterviewSessionDto;
import java.util.List;
import java.util.UUID;

public interface InterviewSessionService {
  UUID initSession(StartInterviewSessionDto sessionDto);

  List<InterviewHistoryDto> getLatestSessions(UUID userId);

  InterviewSessionSummaryDto getSessionSummary(UUID sessionId, UUID userId);

  void cancelSession(UUID sessionId);
}
