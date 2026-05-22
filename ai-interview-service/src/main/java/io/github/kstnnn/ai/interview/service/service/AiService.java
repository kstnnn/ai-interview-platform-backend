package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.dto.AskQuestionDto;
import io.github.kstnnn.ai.interview.service.dto.EvaluationResultDto;
import io.github.kstnnn.ai.interview.service.dto.FollowUpQuestionDto;
import io.github.kstnnn.ai.interview.service.dto.GreetingDto;

public interface AiService {

  String greeting(GreetingDto dto);

  String askQuestion(AskQuestionDto dto);

  EvaluationResultDto evaluateAnswer(
      String question, String expectedAnswer, String candidateAnswer);

  String generateFollowUp(FollowUpQuestionDto dto);
}
