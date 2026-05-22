package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.dto.TopicStateSummaryDto;
import io.github.kstnnn.ai.interview.service.model.SessionTopicState;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicStateService {

  SessionTopicState updateAfterAnswer(
      UUID sessionId, String topic, double totalScore, double aiConfidence, int roundNumber);

  double calculateSessionConfidence(UUID sessionId);

  boolean isSessionComplete(UUID sessionId, int totalQuestionsAsked);

  Optional<String> findWeakestTopic(UUID sessionId);

  List<TopicStateSummaryDto> getTopicSummaries(UUID sessionId);
}
