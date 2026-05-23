package io.github.kstnnn.organization.service.dto;

import java.util.UUID;

public record AiCustomQuestionRequest(
    UUID externalQuestionId,
    String questionText,
    String expectedAnswer,
    String evaluationRubric,
    String topic,
    Integer displayOrder) {}
