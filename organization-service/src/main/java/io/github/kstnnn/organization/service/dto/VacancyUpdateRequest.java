package io.github.kstnnn.organization.service.dto;

import io.github.kstnnn.organization.service.model.EmploymentType;
import io.github.kstnnn.organization.service.model.VacancyLevel;
import io.github.kstnnn.organization.service.model.WorkFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record VacancyUpdateRequest(
    @Size(max = 180) String title,
    String description,
    String requirements,
    @Size(max = 160) String location,
    EmploymentType employmentType,
    WorkFormat workFormat,
    VacancyLevel level,
    List<@NotBlank @Size(max = 64) String> technologyKeys,
    @Min(1) @Max(30) Integer minPrimaryQuestions,
    @Min(1) @Max(30) Integer maxPrimaryQuestions,
    @Min(0) @Max(2) Integer maxFollowUpsPerPrimary) {}
