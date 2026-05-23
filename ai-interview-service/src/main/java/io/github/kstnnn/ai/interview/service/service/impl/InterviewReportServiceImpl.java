package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.dto.InterviewQuestionReportDto;
import io.github.kstnnn.ai.interview.service.dto.InterviewReportDto;
import io.github.kstnnn.ai.interview.service.model.AnswerEvaluation;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionType;
import io.github.kstnnn.ai.interview.service.model.SessionAnswer;
import io.github.kstnnn.ai.interview.service.model.SessionQuestion;
import io.github.kstnnn.ai.interview.service.repository.AnswerEvaluationRepository;
import io.github.kstnnn.ai.interview.service.repository.InterviewSessionRepository;
import io.github.kstnnn.ai.interview.service.repository.SessionAnswerRepository;
import io.github.kstnnn.ai.interview.service.repository.SessionQuestionRepository;
import io.github.kstnnn.ai.interview.service.service.InterviewReportService;
import io.github.kstnnn.ai.interview.service.service.TopicStateService;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewReportServiceImpl implements InterviewReportService {

  private final InterviewSessionRepository iSessionRepository;
  private final SessionQuestionRepository sQuestionRepository;
  private final SessionAnswerRepository sAnswerRepository;
  private final AnswerEvaluationRepository aEvaluationRepository;
  private final TopicStateService topicStateService;

  @Transactional(readOnly = true)
  @Override
  public InterviewReportDto getMockReport(UUID sessionId, UUID userId) {
    var session = iSessionRepository.findById(sessionId).orElseThrow();
    if (!session.getUserId().equals(userId)) {
      throw new IllegalArgumentException("Interview session does not belong to current user");
    }
    if (session.getSessionType() != InterviewSessionType.MOCK) {
      throw new IllegalStateException("Candidate report is available only for mock interviews");
    }
    var topics = topicStateService.getTopicSummaries(sessionId);
    var questions =
        sQuestionRepository.findBySessionIdOrderByRoundNumberAsc(sessionId).stream()
            .map(this::toQuestionReport)
            .toList();

    return new InterviewReportDto(
        session.getId(),
        session.getStatus(),
        session.getFinishedReason(),
        session.getInterviewLevel(),
        session.getInterviewLanguage(),
        topicStateService.calculateSessionConfidence(sessionId),
        session.getStartedAt(),
        session.getFinishedAt(),
        topics,
        questions);
  }

  private InterviewQuestionReportDto toQuestionReport(SessionQuestion question) {
    var answer = sAnswerRepository.findBySessionQuestionId(question.getId()).orElse(null);
    var evaluation = answer == null ? null : findEvaluation(answer);

    return new InterviewQuestionReportDto(
        question.getId(),
        question.getRoundNumber(),
        question.getRoundNumber(),
        question.getQuestionType().toString(),
        question.getSourceType().toString(),
        question.getTopic(),
        question.getSubtopic(),
        question.getDifficulty(),
        question.getQuestionTextSnapshot(),
        answer != null ? answer.getAnswerText() : null,
        evaluation != null ? evaluation.getCorrectnessScore().doubleValue() : null,
        evaluation != null ? evaluation.getDepthScore().doubleValue() : null,
        evaluation != null ? evaluation.getPracticalScore().doubleValue() : null,
        evaluation != null ? evaluation.getTotalScore().doubleValue() : null,
        evaluation != null ? evaluation.getFeedback() : null,
        evaluation != null ? parseKnowledgeGaps(evaluation.getKnowledgeGapsJson()) : List.of());
  }

  private AnswerEvaluation findEvaluation(SessionAnswer answer) {
    return aEvaluationRepository.findBySessionAnswerId(answer.getId()).orElse(null);
  }

  private List<String> parseKnowledgeGaps(String json) {
    if (json == null || json.isBlank() || json.equals("[]")) {
      return List.of();
    }
    var trimmed = json.trim();
    if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
      trimmed = trimmed.substring(1, trimmed.length() - 1);
    }
    if (trimmed.isBlank()) {
      return List.of();
    }
    return Arrays.stream(trimmed.split(","))
        .map(String::trim)
        .map(s -> s.replaceAll("^\\\"|\\\"$", ""))
        .filter(s -> !s.isBlank())
        .toList();
  }
}
