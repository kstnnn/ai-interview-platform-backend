package io.github.kstnnn.organization.service.dto;

import jakarta.validation.Valid;

public record VacancyApplyRequest(String coverLetter, @Valid CandidateContactsDto candidateContacts) {}
