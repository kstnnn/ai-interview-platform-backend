package io.github.kstnnn.ai.interview.service.dto;

import java.util.List;

public record VoiceAnswerResponseDto(
    String transcript,
    String language,
    EvaluationResultDto evaluation,
    NextQuestionResult nextQuestion,
    boolean sessionFinished,
    Double sessionConfidence,
    List<TopicStateSummaryDto> topics) {

  public static VoiceAnswerResponseDto fromSubmission(
      String transcript, String language, AnswerSubmissionResponseDto submission) {
    return new VoiceAnswerResponseDto(
        transcript,
        language,
        submission.evaluation(),
        submission.nextQuestion(),
        submission.sessionFinished(),
        submission.sessionConfidence(),
        submission.topics());
  }
}
