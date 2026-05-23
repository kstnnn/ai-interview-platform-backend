package io.github.kstnnn.organization.service.dto;

import io.github.kstnnn.organization.service.model.VacancyApplicationStatus;
import java.time.Instant;
import java.util.UUID;

public record VacancyApplicationResponse(
    UUID applicationId,
    UUID vacancyId,
    UUID candidateUserId,
    VacancyApplicationStatus status,
    UUID interviewSessionId,
    String coverLetter,
    Instant createdAt,
    Instant updatedAt) {}
