package io.github.kstnnn.ai.interview.service.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record StartInterviewSessionDto(
    UUID user_id, Integer minQuestions, Integer maxQuestions, BigDecimal targetConfidence) {
  public StartInterviewSessionDto {
    if (targetConfidence == null) {
      targetConfidence = new BigDecimal("0.75");
    }
  }
}
