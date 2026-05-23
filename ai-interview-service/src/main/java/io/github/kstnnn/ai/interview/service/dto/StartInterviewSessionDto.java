package io.github.kstnnn.ai.interview.service.dto;

import io.github.kstnnn.ai.interview.service.model.InterviewLevel;
import java.util.List;
import java.util.UUID;

public record StartInterviewSessionDto(
    UUID userId,
    UUID vacancyId,
    UUID applicationId,
    Integer minQuestions,
    Integer maxQuestions,
    InterviewLevel interviewLevel,
    String interviewLanguage,
    List<String> technologyKeys,
    List<CustomInterviewQuestionDto> customQuestions) {}
