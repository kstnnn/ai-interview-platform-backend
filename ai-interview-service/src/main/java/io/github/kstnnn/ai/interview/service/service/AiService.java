package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.dto.GreetingDto;

public interface AiService {

  String greeting(GreetingDto dto);

  void askQuestion();

  void evaluateAnswer();

  void generateFollowUpQuestion();
}
