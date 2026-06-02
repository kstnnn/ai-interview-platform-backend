package io.github.kstnnn.organization.service.dto;

import io.github.kstnnn.organization.service.model.VacancyApplicationStatus;
import java.time.Instant;
import java.util.UUID;

public record EmployerVacancyApplicationResponse(
    UUID applicationId,
    String candidateName,
    CandidateContactsDto candidateContacts,
    VacancyApplicationStatus status,
    Double sessionConfidence,
    String recommendation,
    String coverLetter,
    Instant createdAt,
    Instant completedAt,
    Instant updatedAt) {}
