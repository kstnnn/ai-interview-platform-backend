package io.github.kstnnn.organization.service.dto;

import io.github.kstnnn.organization.service.model.EmploymentType;
import io.github.kstnnn.organization.service.model.VacancyLevel;
import io.github.kstnnn.organization.service.model.WorkFormat;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicVacancyResponse(
    UUID id,
    UUID organizationId,
    String organizationName,
    String title,
    String description,
    String requirements,
    String location,
    EmploymentType employmentType,
    WorkFormat workFormat,
    VacancyLevel level,
    List<String> technologyKeys,
    Integer minPrimaryQuestions,
    Integer maxPrimaryQuestions,
    Integer maxFollowUpsPerPrimary,
    Integer estimatedMaxTotalQuestions,
    Instant createdAt) {}
