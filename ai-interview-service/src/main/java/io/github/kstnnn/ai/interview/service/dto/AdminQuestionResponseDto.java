package io.github.kstnnn.ai.interview.service.dto;

import io.github.kstnnn.ai.interview.service.model.Difficulty;
import io.github.kstnnn.ai.interview.service.model.Question;
import java.time.Instant;
import java.util.UUID;

public record AdminQuestionResponseDto(
    UUID id,
    String externalId,
    AdminTechnologyDto technology,
    String topic,
    String subtopic,
    Difficulty difficulty,
    String questionText,
    String expectedAnswer,
    boolean active,
    Instant createdAt,
    Instant updatedAt) {
  public static AdminQuestionResponseDto toDto(Question question) {
    return new AdminQuestionResponseDto(
        question.getId(),
        question.getExternalId(),
        AdminTechnologyDto.toDto(question.getTechnology()),
        question.getTopic(),
        question.getSubtopic(),
        question.getDifficulty(),
        question.getQuestionText(),
        question.getExpectedAnswer(),
        question.isActive(),
        question.getCreatedAt(),
        question.getUpdatedAt());
  }
}
