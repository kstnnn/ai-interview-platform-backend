package io.github.kstnnn.ai.interview.service.dto;

import io.github.kstnnn.ai.interview.service.model.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminQuestionCreateRequest(
    String externalId,
    @NotBlank String technologyKey,
    @NotBlank String topic,
    String subtopic,
    @NotNull Difficulty difficulty,
    @NotBlank String questionText,
    @NotBlank String expectedAnswer,
    Boolean active) {}
