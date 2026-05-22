package io.github.kstnnn.ai.interview.service.dto;

import io.github.kstnnn.ai.interview.service.model.InterviewLevel;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InterviewHistoryDto(
    UUID sessionId,
    InterviewSessionStatus status,
    InterviewLevel interviewLevel,
    String interviewLanguage,
    List<String> technologyKeys,
    double sessionConfidence,
    Instant startedAt,
    Instant finishedAt,
    long questionsAsked) {}
