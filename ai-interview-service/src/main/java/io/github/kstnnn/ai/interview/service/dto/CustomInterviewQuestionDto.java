package io.github.kstnnn.ai.interview.service.dto;

import java.util.UUID;

public record CustomInterviewQuestionDto(
    UUID externalQuestionId,
    String questionText,
    String expectedAnswer,
    String evaluationRubric,
    String topic,
    Integer displayOrder) {}
