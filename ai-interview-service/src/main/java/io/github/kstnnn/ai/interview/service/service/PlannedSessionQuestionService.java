package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.model.InterviewSession;
import io.github.kstnnn.ai.interview.service.model.Question;
import java.util.List;

public interface PlannedSessionQuestionService {
  void savePlannedQuestions(List<Question> questions, InterviewSession interviewSession);
}
