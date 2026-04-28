package io.github.kstnnn.ai.interview.service.service;

public interface InterviewSessionService {
  String generateFollowUpQuestion(
      String primaryQuestion,
      String expectedAnswer,
      String candidateAnswer,
      String knowledgeGaps,
      String language);
}
