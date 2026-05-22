package io.github.kstnnn.ai.interview.service.dto;

import io.github.kstnnn.ai.interview.service.model.Difficulty;

public record AskQuestionDto(
    String questionText,
    String expectedAnswer,
    String topic,
    String subtopic,
    Difficulty difficulty,
    String interviewLanguage) {

  public String interviewLanguage() {
    return interviewLanguage == null || interviewLanguage.isBlank() ? "Russian" : interviewLanguage;
  }
}
