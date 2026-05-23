package io.github.kstnnn.organization.service.service;

import io.github.kstnnn.organization.service.dto.PublicVacancyResponse;
import io.github.kstnnn.organization.service.dto.VacancyCreateRequest;
import io.github.kstnnn.organization.service.dto.VacancyResponse;
import io.github.kstnnn.organization.service.dto.VacancyUpdateRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

public interface VacancyService {

  VacancyResponse create(Jwt jwt, UUID organizationId, VacancyCreateRequest request);

  List<VacancyResponse> getOrganizationVacancies(Jwt jwt, UUID organizationId);

  VacancyResponse getById(Jwt jwt, UUID vacancyId);

  List<PublicVacancyResponse> getPublishedVacancies();

  PublicVacancyResponse getPublishedById(UUID vacancyId);

  VacancyResponse update(Jwt jwt, UUID vacancyId, VacancyUpdateRequest request);

  VacancyResponse draft(Jwt jwt, UUID vacancyId);

  VacancyResponse publish(Jwt jwt, UUID vacancyId);

  VacancyResponse close(Jwt jwt, UUID vacancyId);
}
