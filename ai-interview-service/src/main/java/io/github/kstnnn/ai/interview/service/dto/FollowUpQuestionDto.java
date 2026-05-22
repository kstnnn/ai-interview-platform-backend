package io.github.kstnnn.ai.interview.service.dto;

import java.util.List;

public record FollowUpQuestionDto(
    String primaryQuestion,
    String expectedAnswer,
    String candidateAnswer,
    List<String> knowledgeGaps,
    String interviewLanguage) {

  public String interviewLanguage() {
    return interviewLanguage == null || interviewLanguage.isBlank() ? "Russian" : interviewLanguage;
  }

  public String knowledgeGapsText() {
    return knowledgeGaps == null || knowledgeGaps.isEmpty()
        ? ""
        : String.join(", ", knowledgeGaps);
  }
}
