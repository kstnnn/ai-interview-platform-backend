package io.github.kstnnn.ai.interview.service.dto;

import io.github.kstnnn.ai.interview.service.model.InterviewLevel;
import java.util.List;
import java.util.UUID;

public record StartInterviewSessionDto(
    UUID userId,
    Integer minQuestions,
    Integer maxQuestions,
    InterviewLevel interviewLevel,
    List<String> technologyKeys) {}
