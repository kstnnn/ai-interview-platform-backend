package io.github.kstnnn.organization.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganizationCreateRequest(
    @NotBlank @Size(max = 160) String name,
    String description,
    @Size(max = 512) String websiteUrl,
    @Size(max = 512) String logoUrl) {}
