package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.dto.CustomInterviewQuestionDto;
import io.github.kstnnn.ai.interview.service.model.Difficulty;
import io.github.kstnnn.ai.interview.service.model.InterviewSession;
import io.github.kstnnn.ai.interview.service.model.PlannedSessionQuestion;
import io.github.kstnnn.ai.interview.service.model.PlannedStatus;
import io.github.kstnnn.ai.interview.service.model.Question;
import io.github.kstnnn.ai.interview.service.model.QuestionSourceType;
import io.github.kstnnn.ai.interview.service.repository.PlannedSessionQuestionRepository;
import io.github.kstnnn.ai.interview.service.service.PlannedSessionQuestionService;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlannedSessionQuestionServiceImpl implements PlannedSessionQuestionService {

  private final PlannedSessionQuestionRepository pRepository;

  @Override
  public void savePlannedQuestions(
      List<CustomInterviewQuestionDto> customQuestions,
      List<Question> questions,
      InterviewSession interviewSession) {
    var plannedCustomQuestions =
        normalizeCustomQuestions(customQuestions).stream()
            .map(q -> toCustomPlannedQuestion(q, interviewSession))
            .toList();
    var baseOrder = plannedCustomQuestions.size() + 1;
    var plannedQuestions =
        IntStream.range(0, questions.size())
            .mapToObj(i -> toQuestionBankPlannedQuestion(questions.get(i), interviewSession, baseOrder + i))
            .toList();
    pRepository.saveAll(plannedCustomQuestions);
    pRepository.saveAll(plannedQuestions);
  }

  private PlannedSessionQuestion toQuestionBankPlannedQuestion(
      Question question, InterviewSession interviewSession, int displayOrder) {
    return PlannedSessionQuestion.builder()
        .question(question)
        .interviewSession(interviewSession)
        .questionTextSnapshot(question.getQuestionText())
        .expectedAnswerSnapshot(question.getExpectedAnswer())
        .topic(question.getTopic())
        .subtopic(question.getSubtopic())
        .difficulty(question.getDifficulty())
        .sourceType(QuestionSourceType.QUESTION_BANK)
        .displayOrder(displayOrder)
        .plannedStatus(PlannedStatus.PLANNED)
        .build();
  }

  private List<CustomInterviewQuestionDto> normalizeCustomQuestions(
      List<CustomInterviewQuestionDto> customQuestions) {
    if (customQuestions == null) {
      return List.of();
    }
    return customQuestions.stream()
        .filter(q -> q.questionText() != null && !q.questionText().isBlank())
        .sorted(
            java.util.Comparator.comparing(
                q -> q.displayOrder() != null ? q.displayOrder() : Integer.MAX_VALUE))
        .toList();
  }

  private PlannedSessionQuestion toCustomPlannedQuestion(
      CustomInterviewQuestionDto question, InterviewSession interviewSession) {
    return PlannedSessionQuestion.builder()
        .interviewSession(interviewSession)
        .questionTextSnapshot(question.questionText())
        .expectedAnswerSnapshot(resolveExpectedAnswer(question))
        .evaluationRubric(question.evaluationRubric())
        .topic(resolveTopic(question.topic()))
        .difficulty(Difficulty.MEDIUM)
        .sourceType(QuestionSourceType.VACANCY_CUSTOM)
        .externalQuestionId(question.externalQuestionId())
        .displayOrder(question.displayOrder() != null ? question.displayOrder() : 0)
        .plannedStatus(PlannedStatus.PLANNED)
        .build();
  }

  private String resolveExpectedAnswer(CustomInterviewQuestionDto question) {
    if (question.evaluationRubric() == null || question.evaluationRubric().isBlank()) {
      return question.expectedAnswer();
    }
    if (question.expectedAnswer() == null || question.expectedAnswer().isBlank()) {
      return question.evaluationRubric();
    }
    return question.expectedAnswer() + "\n\nEvaluation rubric: " + question.evaluationRubric();
  }

  private String resolveTopic(String topic) {
    return topic == null || topic.isBlank() ? "custom" : topic;
  }
}
