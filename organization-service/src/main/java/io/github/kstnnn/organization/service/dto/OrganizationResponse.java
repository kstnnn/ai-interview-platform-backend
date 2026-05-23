package io.github.kstnnn.organization.service.dto;

import io.github.kstnnn.organization.service.model.OrganizationStatus;
import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(
    UUID id,
    UUID ownerUserId,
    String name,
    String description,
    String websiteUrl,
    String logoUrl,
    OrganizationStatus status,
    Instant createdAt,
    Instant updatedAt) {}
