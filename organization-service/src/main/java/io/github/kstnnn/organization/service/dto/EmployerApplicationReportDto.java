package io.github.kstnnn.organization.service.dto;

import io.github.kstnnn.organization.service.model.VacancyApplicationStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EmployerApplicationReportDto(
    UUID applicationId,
    UUID vacancyId,
    UUID interviewSessionId,
    EmployerCandidateDto candidate,
    VacancyApplicationStatus status,
    Double sessionConfidence,
    String recommendation,
    List<AiTopicStateSummaryDto> topics,
    List<AiInterviewQuestionReportDto> questions,
    Instant createdAt,
    Instant completedAt) {}
