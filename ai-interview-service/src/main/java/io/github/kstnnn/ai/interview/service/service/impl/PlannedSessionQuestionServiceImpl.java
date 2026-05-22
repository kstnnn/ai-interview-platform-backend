package io.github.kstnnn.ai.interview.service.service.impl;

import io.github.kstnnn.ai.interview.service.model.InterviewSession;
import io.github.kstnnn.ai.interview.service.model.PlannedSessionQuestion;
import io.github.kstnnn.ai.interview.service.model.PlannedStatus;
import io.github.kstnnn.ai.interview.service.model.Question;
import io.github.kstnnn.ai.interview.service.repository.PlannedSessionQuestionRepository;
import io.github.kstnnn.ai.interview.service.service.PlannedSessionQuestionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlannedSessionQuestionServiceImpl implements PlannedSessionQuestionService {

  private final PlannedSessionQuestionRepository pRepository;

  @Override
  public void savePlannedQuestions(List<Question> questions, InterviewSession interviewSession) {
    var plannedQuestions =
        questions.stream()
            .map(
                q ->
                    PlannedSessionQuestion.builder()
                        .question(q)
                        .interviewSession(interviewSession)
                        .plannedStatus(PlannedStatus.PLANNED)
                        .build())
            .toList();
    pRepository.saveAll(plannedQuestions);
  }
}
