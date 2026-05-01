package io.github.kstnnn.ai.interview.service.service;

public interface QuestionImportService {

  void loadQuestionsIntoDb();

  void loadQuestionsIntoVectorStore();
}
