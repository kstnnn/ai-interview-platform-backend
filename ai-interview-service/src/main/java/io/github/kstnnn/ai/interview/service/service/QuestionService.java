package io.github.kstnnn.ai.interview.service.service;

import io.github.kstnnn.ai.interview.service.model.Question;
import io.github.kstnnn.ai.interview.service.model.Technology;
import java.util.List;

public interface QuestionService {

  List<Question> getBaseQuestions(List<Technology> technologies);

  List<Question> getQuestionsPerTechnology(String technology);
}
