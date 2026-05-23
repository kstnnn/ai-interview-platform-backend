package io.github.kstnnn.organization.service.controller;

import io.github.kstnnn.organization.service.dto.VacancyApplicationResponse;
import io.github.kstnnn.organization.service.service.VacancyApplicationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {

  private final VacancyApplicationService vacancyApplicationService;

  @GetMapping("/my")
  public List<VacancyApplicationResponse> getMyApplications(@AuthenticationPrincipal Jwt jwt) {
    return vacancyApplicationService.getMyApplications(jwt);
  }

  @GetMapping("/{applicationId}")
  public VacancyApplicationResponse getMyApplication(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID applicationId) {
    return vacancyApplicationService.getMyApplication(jwt, applicationId);
  }
}
