package io.github.kstnnn.ai.interview.service.dto;

import io.github.kstnnn.ai.interview.service.model.InterviewLevel;
import io.github.kstnnn.ai.interview.service.model.InterviewSessionStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SessionCreatedResponse(
    UUID sessionId,
    InterviewSessionStatus status,
    InterviewLevel interviewLevel,
    Integer minQuestions,
    Integer maxQuestions,
    List<String> technologyKeys,
    Instant createdAt) {}
