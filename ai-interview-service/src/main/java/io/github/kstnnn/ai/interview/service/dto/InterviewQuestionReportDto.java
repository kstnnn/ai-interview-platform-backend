package io.github.kstnnn.ai.interview.service.dto;

import io.github.kstnnn.ai.interview.service.model.Difficulty;
import java.util.List;
import java.util.UUID;

public record InterviewQuestionReportDto(
    UUID sessionQuestionId,
    int roundNumber,
    int questionIndex,
    String questionType,
    String sourceType,
    String topic,
    String subtopic,
    Difficulty difficulty,
    String questionText,
    String answerText,
    Double correctnessScore,
    Double depthScore,
    Double practicalScore,
    Double totalScore,
    String feedback,
    List<String> knowledgeGaps) {}
