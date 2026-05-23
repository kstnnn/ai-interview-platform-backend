package io.github.kstnnn.organization.service.dto;

import java.time.Instant;
import java.util.UUID;

public record VacancyQuestionResponse(
    UUID id,
    UUID vacancyId,
    String questionText,
    String expectedAnswer,
    String evaluationRubric,
    String topic,
    boolean required,
    int displayOrder,
    boolean active,
    Instant createdAt,
    Instant updatedAt) {}
