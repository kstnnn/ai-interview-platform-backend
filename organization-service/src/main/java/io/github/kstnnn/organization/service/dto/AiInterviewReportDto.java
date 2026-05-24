package io.github.kstnnn.organization.service.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AiInterviewReportDto(
    UUID sessionId,
    String status,
    String finishedReason,
    String interviewLevel,
    String interviewLanguage,
    Double sessionConfidence,
    Instant startedAt,
    Instant finishedAt,
    List<AiTopicStateSummaryDto> topics,
    List<AiInterviewQuestionReportDto> questions) {}
