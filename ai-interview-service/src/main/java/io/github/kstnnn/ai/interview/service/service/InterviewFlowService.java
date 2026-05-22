package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.dto.EvaluationResultDto;
import io.github.kstnnn.ai.interview.service.dto.NextQuestionResult;
import io.github.kstnnn.ai.interview.service.dto.SubmitAnswerDto;
import io.github.kstnnn.ai.interview.service.model.InterviewFinishedReason;
import java.util.UUID;

public interface InterviewFlowService {

  void startSession(UUID sessionId, String interviewLanguage);

  EvaluationResultDto submitAnswer(UUID sessionId, SubmitAnswerDto dto);

  NextQuestionResult decideNextQuestion(UUID sessionId, EvaluationResultDto evaluation);

  NextQuestionResult askFirstQuestion(UUID sessionId);

  void finishSession(UUID sessionId, InterviewFinishedReason reason);

  void cancelSession(UUID sessionId);
}
