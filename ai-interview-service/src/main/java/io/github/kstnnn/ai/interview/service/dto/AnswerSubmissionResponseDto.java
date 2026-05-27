package io.github.kstnnn.ai.interview.service.dto;

import java.util.List;

public record AnswerSubmissionResponseDto(
    EvaluationResultDto evaluation,
    NextQuestionResult nextQuestion,
    boolean sessionFinished,
    Double sessionConfidence,
    List<TopicStateSummaryDto> topics) {}
