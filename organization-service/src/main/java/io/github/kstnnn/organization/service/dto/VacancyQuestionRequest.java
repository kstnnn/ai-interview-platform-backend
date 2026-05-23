package io.github.kstnnn.organization.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VacancyQuestionRequest(
    @NotBlank String questionText,
    String expectedAnswer,
    String evaluationRubric,
    @Size(max = 80) String topic,
    Boolean required,
    Integer displayOrder) {}
