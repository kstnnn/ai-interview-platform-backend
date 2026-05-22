package io.github.kstnnn.ai.interview.service.dto;

import java.util.UUID;

public record NextQuestionResult(
    UUID sessionQuestionId,
    String questionText,
    int roundNumber,
    String questionType,
    String candidateFeedback,
    boolean isFollowUp) {}
