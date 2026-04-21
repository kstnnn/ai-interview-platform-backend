package io.github.kstnnn.ai.interview.service.dto;

import jakarta.validation.constraints.NotBlank;

public record FollowUpQuestionRequestDto(
    @NotBlank(message = "Primary question is required") String primaryQuestion,
    @NotBlank(message = "Expected answer is required") String expectedAnswer,
    @NotBlank(message = "Candidate answer is required") String candidateAnswer,
    String knowledgeGaps,
    String interviewLanguage) {

  public String knowledgeGaps() {
    return knowledgeGaps == null ? "" : knowledgeGaps;
  }

  public String interviewLanguage() {
    return interviewLanguage == null || interviewLanguage.isBlank() ? "English" : interviewLanguage;
  }
}
