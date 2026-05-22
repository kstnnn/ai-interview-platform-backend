package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.dto.TopicStateSummaryDto;
import io.github.kstnnn.ai.interview.service.model.InterviewSession;
import io.github.kstnnn.ai.interview.service.model.SessionTopicState;
import io.github.kstnnn.ai.interview.service.repository.InterviewSessionRepository;
import io.github.kstnnn.ai.interview.service.repository.SessionTopicStateRepository;
import io.github.kstnnn.ai.interview.service.service.TopicStateService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicStateServiceImpl implements TopicStateService {

  private static final double MASTERY_ALPHA = 0.7;
  private static final double CONFIDENCE_QUESTIONS_CAP = 4.0;

  private final SessionTopicStateRepository topicStateRepository;
  private final InterviewSessionRepository sessionRepository;

  @Transactional
  @Override
  public SessionTopicState updateAfterAnswer(
      UUID sessionId, String topic, double totalScore, double aiConfidence, int roundNumber) {
    var state =
        topicStateRepository
            .findBySessionIdAndTopic(sessionId, topic)
            .orElseGet(() -> createNewState(sessionId, topic));

    double normalized = clamp01(totalScore);
    int newQuestionsAsked = state.getQuestionsAsked() + 1;
    state.setQuestionsAsked(newQuestionsAsked);
    state.setLastAskedRound(roundNumber);

    if (newQuestionsAsked == 1) {
      state.setMasteryScore(BigDecimal.valueOf(normalized));
    } else {
      double previousMastery = state.getMasteryScore().doubleValue();
      double updatedMastery = MASTERY_ALPHA * previousMastery + (1 - MASTERY_ALPHA) * normalized;
      state.setMasteryScore(BigDecimal.valueOf(updatedMastery));
    }

    double confidence = Math.min(1.0, newQuestionsAsked / CONFIDENCE_QUESTIONS_CAP);
    state.setConfidenceScore(BigDecimal.valueOf(confidence));

    double prevAvg = state.getAvgScore().doubleValue();
    double newAvg = (prevAvg * (newQuestionsAsked - 1) + normalized) / newQuestionsAsked;
    state.setAvgScore(BigDecimal.valueOf(newAvg));

    return topicStateRepository.save(state);
  }

  @Override
  public double calculateSessionConfidence(UUID sessionId) {
    var states = topicStateRepository.findAllBySessionId(sessionId);
    if (states.isEmpty()) {
      return 0.0;
    }

    double sum = 0.0;
    int count = 0;
    for (var state : states) {
      if (state.getQuestionsAsked() > 0) {
        sum += state.getMasteryScore().doubleValue() * state.getConfidenceScore().doubleValue();
        count++;
      }
    }
    return count > 0 ? sum / count : 0.0;
  }

  @Override
  public boolean isSessionComplete(UUID sessionId, int totalQuestionsAsked) {
    var session = sessionRepository.findById(sessionId).orElseThrow();
    var maxQuestions = session.getMaxQuestions();
    var minQuestions = session.getMinQuestions();
    var minPerTopic = session.getMinQuestionsPerTopic();
    var targetConfidence = session.getTargetConfidence().doubleValue();

    if (totalQuestionsAsked >= maxQuestions) {
      return true;
    }

    if (totalQuestionsAsked < minQuestions) {
      return false;
    }

    var states = topicStateRepository.findAllBySessionId(sessionId);
    for (var state : states) {
      if (state.getQuestionsAsked() < minPerTopic) {
        return false;
      }
    }

    double sessionConfidence = calculateSessionConfidence(sessionId);
    if (sessionConfidence < targetConfidence) {
      return false;
    }

    return true;
  }

  @Override
  public Optional<String> findWeakestTopic(UUID sessionId) {
    var session = sessionRepository.findById(sessionId).orElseThrow();
    var maxPerTopic = session.getMaxFollowUpsPerPrimary();

    var states = topicStateRepository.findAllBySessionId(sessionId);
    return states.stream()
        .filter(s -> s.getQuestionsAsked() < maxPerTopic)
        .min((a, b) -> a.getMasteryScore().compareTo(b.getMasteryScore()))
        .map(SessionTopicState::getTopic);
  }

  @Override
  public List<TopicStateSummaryDto> getTopicSummaries(UUID sessionId) {
    return topicStateRepository.findAllBySessionId(sessionId).stream()
        .map(
            s ->
                new TopicStateSummaryDto(
                    s.getTopic(),
                    s.getQuestionsAsked(),
                    s.getMasteryScore(),
                    s.getConfidenceScore(),
                    s.getAvgScore()))
        .toList();
  }

  private SessionTopicState createNewState(UUID sessionId, String topic) {
    var session = sessionRepository.findById(sessionId).orElseThrow();
    var state = new SessionTopicState();
    state.setSession(session);
    state.setTopic(topic);
    state.setQuestionsAsked(0);
    state.setAvgScore(BigDecimal.ZERO);
    state.setMasteryScore(BigDecimal.ZERO);
    state.setConfidenceScore(BigDecimal.ZERO);
    return state;
  }

  private double clamp01(double value) {
    return Math.max(0.0, Math.min(1.0, value));
  }
}
