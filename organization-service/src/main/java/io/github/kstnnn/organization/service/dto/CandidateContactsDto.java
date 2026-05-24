package io.github.kstnnn.organization.service.dto;

import jakarta.validation.constraints.Email;

public record CandidateContactsDto(
    @Email String email,
    String phone,
    String telegram,
    String linkedIn,
    String portfolioUrl,
    String hhResumeUrl) {}
