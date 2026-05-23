package io.github.kstnnn.organization.service.dto;

import java.util.List;
import java.util.UUID;

public record AiStartInterviewRequest(
    UUID userId,
    UUID vacancyId,
    UUID applicationId,
    Integer minQuestions,
    Integer maxQuestions,
    Integer maxFollowUpsPerPrimary,
    String interviewLevel,
    String interviewLanguage,
    List<String> technologyKeys,
    List<AiCustomQuestionRequest> customQuestions) {}
