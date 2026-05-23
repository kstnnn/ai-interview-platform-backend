package io.github.kstnnn.organization.service.controller;

import io.github.kstnnn.organization.service.dto.OrganizationCreateRequest;
import io.github.kstnnn.organization.service.dto.OrganizationResponse;
import io.github.kstnnn.organization.service.dto.OrganizationUpdateRequest;
import io.github.kstnnn.organization.service.dto.VacancyCreateRequest;
import io.github.kstnnn.organization.service.dto.VacancyResponse;
import io.github.kstnnn.organization.service.service.OrganizationService;
import io.github.kstnnn.organization.service.service.VacancyService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

  private final OrganizationService organizationService;
  private final VacancyService vacancyService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrganizationResponse create(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody OrganizationCreateRequest request) {
    return organizationService.create(jwt, request);
  }

  @GetMapping("/my")
  public List<OrganizationResponse> getMyOrganizations(@AuthenticationPrincipal Jwt jwt) {
    return organizationService.getMyOrganizations(jwt);
  }

  @GetMapping("/{organizationId}")
  public OrganizationResponse getById(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID organizationId) {
    return organizationService.getById(jwt, organizationId);
  }

  @PatchMapping("/{organizationId}")
  public OrganizationResponse update(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID organizationId,
      @Valid @RequestBody OrganizationUpdateRequest request) {
    return organizationService.update(jwt, organizationId, request);
  }

  @PostMapping("/{organizationId}/vacancies")
  @ResponseStatus(HttpStatus.CREATED)
  public VacancyResponse createVacancy(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID organizationId,
      @Valid @RequestBody VacancyCreateRequest request) {
    return vacancyService.create(jwt, organizationId, request);
  }

  @GetMapping("/{organizationId}/vacancies")
  public List<VacancyResponse> getVacancies(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID organizationId) {
    return vacancyService.getOrganizationVacancies(jwt, organizationId);
  }
}
