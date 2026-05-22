package io.github.kstnnn.ai.interview.service.dto;

import io.github.kstnnn.ai.interview.service.model.InterviewFinishedReason;
import io.github.kstnnn.ai.interview.service.model.InterviewLevel;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InterviewReportDto(
    UUID sessionId,
    InterviewSessionStatus status,
    InterviewFinishedReason finishedReason,
    InterviewLevel interviewLevel,
    String interviewLanguage,
    double sessionConfidence,
    Instant startedAt,
    Instant finishedAt,
    List<TopicStateSummaryDto> topics,
    List<InterviewQuestionReportDto> questions) {}
