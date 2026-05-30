package io.github.kstnnn.organization.service.controller;

import io.github.kstnnn.organization.service.dto.PublicVacancyResponse;
import io.github.kstnnn.organization.service.dto.EmployerApplicationReportDto;
import io.github.kstnnn.organization.service.dto.EmployerVacancyApplicationResponse;
import io.github.kstnnn.organization.service.dto.VacancyApplicationResponse;
import io.github.kstnnn.organization.service.dto.VacancyApplyRequest;
import io.github.kstnnn.organization.service.dto.VacancyQuestionRequest;
import io.github.kstnnn.organization.service.dto.VacancyQuestionResponse;
import io.github.kstnnn.organization.service.dto.VacancyResponse;
import io.github.kstnnn.organization.service.dto.VacancyUpdateRequest;
import io.github.kstnnn.organization.service.service.VacancyApplicationService;
import io.github.kstnnn.organization.service.service.VacancyQuestionService;
import io.github.kstnnn.organization.service.service.VacancyService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vacancies")
@RequiredArgsConstructor
public class VacancyController {

  private final VacancyService vacancyService;
  private final VacancyQuestionService vacancyQuestionService;
  private final VacancyApplicationService vacancyApplicationService;

  @GetMapping("/public")
  public List<PublicVacancyResponse> getPublishedVacancies() {
    return vacancyService.getPublishedVacancies();
  }

  @GetMapping("/public/{vacancyId}")
  public PublicVacancyResponse getPublishedById(@PathVariable UUID vacancyId) {
    return vacancyService.getPublishedById(vacancyId);
  }

  @PostMapping("/public/{vacancyId}/applications")
  @ResponseStatus(HttpStatus.CREATED)
  public VacancyApplicationResponse apply(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID vacancyId,
      @Valid @RequestBody VacancyApplyRequest request) {
    return vacancyApplicationService.apply(jwt, vacancyId, request);
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

  @PostMapping("/{vacancyId}/questions")
  @ResponseStatus(HttpStatus.CREATED)
  public VacancyQuestionResponse createQuestion(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID vacancyId,
      @Valid @RequestBody VacancyQuestionRequest request) {
    return vacancyQuestionService.create(jwt, vacancyId, request);
  }

  @GetMapping("/{vacancyId}/questions")
  public List<VacancyQuestionResponse> getQuestions(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID vacancyId) {
    return vacancyQuestionService.list(jwt, vacancyId);
  }

  @PatchMapping("/{vacancyId}/questions/{questionId}")
  public VacancyQuestionResponse updateQuestion(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID vacancyId,
      @PathVariable UUID questionId,
      @Valid @RequestBody VacancyQuestionRequest request) {
    return vacancyQuestionService.update(jwt, vacancyId, questionId, request);
  }

  @DeleteMapping("/{vacancyId}/questions/{questionId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteQuestion(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID vacancyId, @PathVariable UUID questionId) {
    vacancyQuestionService.delete(jwt, vacancyId, questionId);
  }

  @GetMapping("/{vacancyId}/applications")
  public List<EmployerVacancyApplicationResponse> getApplications(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID vacancyId) {
    return vacancyApplicationService.getVacancyApplications(jwt, vacancyId);
  }

  @GetMapping(value = "/{vacancyId}/applications/export", produces = "text/csv")
  public ResponseEntity<String> exportApplications(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID vacancyId) {
    var csv = vacancyApplicationService.exportVacancyApplicationsCsv(jwt, vacancyId);
    return ResponseEntity.ok()
        .contentType(new MediaType("text", "csv"))
        .header(HttpHeaders.CONTENT_DISPOSITION, attachment("vacancy-applications-%s.csv".formatted(vacancyId)))
        .body(csv);
  }

  @GetMapping("/{vacancyId}/applications/{applicationId}/report")
  public EmployerApplicationReportDto getApplicationReport(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID vacancyId,
      @PathVariable UUID applicationId) {
    return vacancyApplicationService.getEmployerReport(jwt, vacancyId, applicationId);
  }

  @GetMapping(value = "/{vacancyId}/applications/{applicationId}/report/export", produces = "application/pdf")
  public ResponseEntity<byte[]> exportApplicationReport(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID vacancyId,
      @PathVariable UUID applicationId,
      @org.springframework.web.bind.annotation.RequestParam(required = false) String language,
      @org.springframework.web.bind.annotation.RequestHeader(name = "Accept-Language", required = false) String acceptLanguage) {
    var pdf = vacancyApplicationService.exportEmployerReportPdf(
        jwt, vacancyId, applicationId, language != null && !language.isBlank() ? language : acceptLanguage);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, attachment("application-report-%s.pdf".formatted(applicationId)))
        .body(pdf);
  }

  private String attachment(String filename) {
    return ContentDisposition.attachment().filename(filename).build().toString();
  }
}
