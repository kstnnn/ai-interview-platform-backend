package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.model.Difficulty;
import io.github.kstnnn.ai.interview.service.model.Question;
import java.util.List;
import java.util.Optional;

public interface QuestionService {

  List<Question> getBaseQuestions(List<String> technologyKeys);

  Optional<Question> findReinforcementQuestion(
      String topic, Difficulty difficulty, List<String> excludeExternalIds);
}
