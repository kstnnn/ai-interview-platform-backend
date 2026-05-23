package io.github.kstnnn.organization.service.controller;

import io.github.kstnnn.organization.service.dto.PublicVacancyResponse;
import io.github.kstnnn.organization.service.dto.VacancyResponse;
import io.github.kstnnn.organization.service.dto.VacancyUpdateRequest;
import io.github.kstnnn.organization.service.service.VacancyService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vacancies")
@RequiredArgsConstructor
public class VacancyController {

  private final VacancyService vacancyService;

  @GetMapping("/public")
  public List<PublicVacancyResponse> getPublishedVacancies() {
    return vacancyService.getPublishedVacancies();
  }

  @GetMapping("/public/{vacancyId}")
  public PublicVacancyResponse getPublishedById(@PathVariable UUID vacancyId) {
    return vacancyService.getPublishedById(vacancyId);
  }

  @GetMapping("/{vacancyId}")
  public VacancyResponse getById(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID vacancyId) {
    return vacancyService.getById(jwt, vacancyId);
  }

  @PatchMapping("/{vacancyId}")
  public VacancyResponse update(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID vacancyId,
      @Valid @RequestBody VacancyUpdateRequest request) {
    return vacancyService.update(jwt, vacancyId, request);
  }

  @PostMapping("/{vacancyId}/draft")
  public VacancyResponse draft(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID vacancyId) {
    return vacancyService.draft(jwt, vacancyId);
  }

  @PostMapping("/{vacancyId}/publish")
  public VacancyResponse publish(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID vacancyId) {
    return vacancyService.publish(jwt, vacancyId);
  }

  @PostMapping("/{vacancyId}/close")
  public VacancyResponse close(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID vacancyId) {
    return vacancyService.close(jwt, vacancyId);
  }
}
