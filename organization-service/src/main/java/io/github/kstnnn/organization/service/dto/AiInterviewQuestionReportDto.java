package io.github.kstnnn.organization.service.dto;

import java.util.List;
import java.util.UUID;

public record AiInterviewQuestionReportDto(
    UUID sessionQuestionId,
    int roundNumber,
    int questionIndex,
    String questionType,
    String sourceType,
    String topic,
    String subtopic,
    String difficulty,
    String questionText,
    String answerText,
    Double correctnessScore,
    Double depthScore,
    Double practicalScore,
    Double totalScore,
    String feedback,
    List<String> knowledgeGaps) {}
