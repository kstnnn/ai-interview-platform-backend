package io.github.kstnnn.ai.interview.service.dto;

import io.github.kstnnn.ai.interview.service.model.InterviewLevel;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionStatus;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionType;
import java.time.Instant;
import java.util.UUID;

public record InterviewSessionSummaryDto(
    UUID sessionId,
    InterviewSessionType sessionType,
    InterviewSessionStatus status,
    InterviewLevel interviewLevel,
    String interviewLanguage,
    UUID vacancyId,
    UUID applicationId,
    Instant startedAt,
    Instant finishedAt) {}
