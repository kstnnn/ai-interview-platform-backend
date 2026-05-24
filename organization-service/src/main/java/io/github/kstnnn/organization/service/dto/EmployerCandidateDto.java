package io.github.kstnnn.organization.service.dto;

import java.util.UUID;

public record EmployerCandidateDto(
    UUID userId, String firstName, String lastName, String email, CandidateContactsDto contacts) {}
