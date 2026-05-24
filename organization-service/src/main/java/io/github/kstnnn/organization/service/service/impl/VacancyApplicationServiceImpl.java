package io.github.kstnnn.organization.service.service.impl;

import io.github.kstnnn.organization.service.dto.AiCustomQuestionRequest;
import io.github.kstnnn.organization.service.dto.AiInterviewReportDto;
import io.github.kstnnn.organization.service.dto.AiStartInterviewRequest;
import io.github.kstnnn.organization.service.dto.CandidateContactsDto;
import io.github.kstnnn.organization.service.dto.EmployerApplicationReportDto;
import io.github.kstnnn.organization.service.dto.EmployerCandidateDto;
import io.github.kstnnn.organization.service.dto.VacancyApplicationResponse;
import io.github.kstnnn.organization.service.dto.VacancyApplyRequest;
import io.github.kstnnn.organization.service.exception.DuplicateApplicationException;
import io.github.kstnnn.organization.service.exception.InvalidApplicationRequestException;
import io.github.kstnnn.organization.service.exception.ResourceNotFoundException;
import io.github.kstnnn.organization.service.model.Vacancy;
import io.github.kstnnn.organization.service.model.VacancyApplication;
import io.github.kstnnn.organization.service.model.VacancyApplicationStatus;
import io.github.kstnnn.organization.service.model.VacancyStatus;
import io.github.kstnnn.organization.service.repository.VacancyApplicationRepository;
import io.github.kstnnn.organization.service.repository.VacancyQuestionRepository;
import io.github.kstnnn.organization.service.repository.VacancyRepository;
import io.github.kstnnn.organization.service.repository.VacancyTechnologyRepository;
import io.github.kstnnn.organization.service.service.AiInterviewClient;
import io.github.kstnnn.organization.service.service.CurrentUserService;
import io.github.kstnnn.organization.service.service.OrganizationAccessService;
import io.github.kstnnn.organization.service.service.VacancyApplicationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class VacancyApplicationServiceImpl implements VacancyApplicationService {

  private final VacancyApplicationRepository vacancyApplicationRepository;
  private final VacancyRepository vacancyRepository;
  private final VacancyQuestionRepository vacancyQuestionRepository;
  private final VacancyTechnologyRepository vacancyTechnologyRepository;
  private final CurrentUserService currentUserService;
  private final OrganizationAccessService organizationAccessService;
  private final AiInterviewClient aiInterviewClient;

  @Override
  @Transactional
  public VacancyApplicationResponse apply(Jwt jwt, UUID vacancyId, VacancyApplyRequest request) {
    var candidate = currentUserService.requireActiveCandidateUser(jwt);
    var vacancy = loadPublishedVacancy(vacancyId);
    var contacts = requireContacts(request);
    if (vacancyApplicationRepository.existsByVacancyIdAndCandidateUserId(vacancyId, candidate.id())) {
      throw new DuplicateApplicationException();
    }
    var application =
        vacancyApplicationRepository.save(
            VacancyApplication.builder()
                .vacancy(vacancy)
                .candidateUserId(candidate.id())
                .candidateFirstName(candidate.firstName())
                .candidateLastName(candidate.lastName())
                .candidateEmail(candidate.email())
                .contactEmail(contacts.email())
                .contactPhone(contacts.phone())
                .contactTelegram(contacts.telegram())
                .contactLinkedIn(contacts.linkedIn())
                .contactPortfolioUrl(contacts.portfolioUrl())
                .contactHhResumeUrl(contacts.hhResumeUrl())
                .coverLetter(trimToNull(request.coverLetter()))
                .status(VacancyApplicationStatus.INTERVIEW_CREATED)
                .build());

    var interview = aiInterviewClient.createSession(toInterviewRequest(vacancy, application));
    application.setInterviewSessionId(interview.sessionId());
    return toResponse(application);
  }

  @Override
  @Transactional(readOnly = true)
  public List<VacancyApplicationResponse> getMyApplications(Jwt jwt) {
    var candidate = currentUserService.requireActiveCandidateUser(jwt);
    return vacancyApplicationRepository.findByCandidateUserIdOrderByCreatedAtDesc(candidate.id()).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public VacancyApplicationResponse getMyApplication(Jwt jwt, UUID applicationId) {
    var candidate = currentUserService.requireActiveCandidateUser(jwt);
    return vacancyApplicationRepository
        .findByIdAndCandidateUserId(applicationId, candidate.id())
        .map(this::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
  }

  @Override
  @Transactional(readOnly = true)
  public List<VacancyApplicationResponse> getVacancyApplications(Jwt jwt, UUID vacancyId) {
    var user = currentUserService.requireActiveBusinessUser(jwt);
    var vacancy =
        vacancyRepository
            .findById(vacancyId)
            .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));
    organizationAccessService.requireWritableMember(vacancy.getOrganization().getId(), user.id());
    return vacancyApplicationRepository.findByVacancyIdOrderByCreatedAtDesc(vacancyId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public EmployerApplicationReportDto getEmployerReport(Jwt jwt, UUID vacancyId, UUID applicationId) {
    var user = currentUserService.requireActiveBusinessUser(jwt);
    var application = loadApplication(vacancyId, applicationId);
    organizationAccessService.requireWritableMember(application.getVacancy().getOrganization().getId(), user.id());
    var report = aiInterviewClient.getReport(application.getInterviewSessionId());
    return new EmployerApplicationReportDto(
        application.getId(),
        application.getVacancy().getId(),
        application.getInterviewSessionId(),
        toCandidate(application),
        effectiveStatus(application, report),
        report.sessionConfidence(),
        recommendation(report.sessionConfidence()),
        report.topics(),
        report.questions(),
        application.getCreatedAt(),
        report.finishedAt());
  }

  private VacancyApplication loadApplication(UUID vacancyId, UUID applicationId) {
    var application =
        vacancyApplicationRepository
            .findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    if (!application.getVacancy().getId().equals(vacancyId)) {
      throw new ResourceNotFoundException("Application not found");
    }
    return application;
  }

  private Vacancy loadPublishedVacancy(UUID vacancyId) {
    var vacancy =
        vacancyRepository
            .findById(vacancyId)
            .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));
    if (vacancy.getStatus() != VacancyStatus.PUBLISHED) {
      throw new ResourceNotFoundException("Vacancy not found");
    }
    return vacancy;
  }

  private AiStartInterviewRequest toInterviewRequest(Vacancy vacancy, VacancyApplication application) {
    var technologies = vacancyTechnologyRepository.findTechnologyKeysByVacancyId(vacancy.getId());
    var customQuestions =
        vacancyQuestionRepository.findByVacancyIdAndActiveTrueOrderByDisplayOrderAsc(vacancy.getId()).stream()
            .map(
                q ->
                    new AiCustomQuestionRequest(
                        q.getId(),
                        q.getQuestionText(),
                        q.getExpectedAnswer(),
                        q.getEvaluationRubric(),
                        q.getTopic(),
                        q.getDisplayOrder()))
            .toList();
    var minPrimaryQuestions = Math.max(vacancy.getMinPrimaryQuestions(), customQuestions.size());
    var maxPrimaryQuestions = Math.max(vacancy.getMaxPrimaryQuestions(), minPrimaryQuestions);
    return new AiStartInterviewRequest(
        application.getCandidateUserId(),
        vacancy.getId(),
        application.getId(),
        minPrimaryQuestions,
        maxPrimaryQuestions,
        vacancy.getMaxFollowUpsPerPrimary(),
        vacancy.getLevel().name(),
        "Russian",
        technologies,
        customQuestions);
  }

  private VacancyApplicationResponse toResponse(VacancyApplication application) {
    var report = safeReport(application.getInterviewSessionId());
    return new VacancyApplicationResponse(
        application.getId(),
        application.getVacancy().getId(),
        application.getCandidateUserId(),
        candidateName(application),
        toContacts(application),
        effectiveStatus(application, report),
        application.getInterviewSessionId(),
        report != null ? report.sessionConfidence() : null,
        report != null ? recommendation(report.sessionConfidence()) : null,
        application.getCoverLetter(),
        application.getCreatedAt(),
        report != null ? report.finishedAt() : null,
        application.getUpdatedAt());
  }

  private AiInterviewReportDto safeReport(UUID interviewSessionId) {
    if (interviewSessionId == null) {
      return null;
    }
    try {
      return aiInterviewClient.getReport(interviewSessionId);
    } catch (RestClientException ex) {
      return null;
    }
  }

  private VacancyApplicationStatus effectiveStatus(
      VacancyApplication application, AiInterviewReportDto report) {
    if (report != null && "COMPLETED".equals(report.status())) {
      return VacancyApplicationStatus.INTERVIEW_COMPLETED;
    }
    if (report != null && "IN_PROGRESS".equals(report.status())) {
      return VacancyApplicationStatus.INTERVIEW_IN_PROGRESS;
    }
    return application.getStatus();
  }

  private String recommendation(Double sessionConfidence) {
    if (sessionConfidence == null) {
      return null;
    }
    if (sessionConfidence >= 0.90) return "STRONG_YES";
    if (sessionConfidence >= 0.75) return "YES";
    if (sessionConfidence >= 0.60) return "MAYBE";
    if (sessionConfidence >= 0.40) return "NO";
    return "STRONG_NO";
  }

  private EmployerCandidateDto toCandidate(VacancyApplication application) {
    return new EmployerCandidateDto(
        application.getCandidateUserId(),
        application.getCandidateFirstName(),
        application.getCandidateLastName(),
        application.getCandidateEmail(),
        toContacts(application));
  }

  private CandidateContactsDto requireContacts(VacancyApplyRequest request) {
    if (request == null || request.candidateContacts() == null) {
      throw new InvalidApplicationRequestException("At least one candidate contact method is required");
    }
    var contacts =
        new CandidateContactsDto(
            trimToNull(request.candidateContacts().email()),
            trimToNull(request.candidateContacts().phone()),
            trimToNull(request.candidateContacts().telegram()),
            trimToNull(request.candidateContacts().linkedIn()),
            trimToNull(request.candidateContacts().portfolioUrl()),
            trimToNull(request.candidateContacts().hhResumeUrl()));
    if (contacts.email() == null
        && contacts.phone() == null
        && contacts.telegram() == null
        && contacts.linkedIn() == null
        && contacts.portfolioUrl() == null
        && contacts.hhResumeUrl() == null) {
      throw new InvalidApplicationRequestException("At least one candidate contact method is required");
    }
    return contacts;
  }

  private CandidateContactsDto toContacts(VacancyApplication application) {
    return new CandidateContactsDto(
        application.getContactEmail(),
        application.getContactPhone(),
        application.getContactTelegram(),
        application.getContactLinkedIn(),
        application.getContactPortfolioUrl(),
        application.getContactHhResumeUrl());
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    var trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String candidateName(VacancyApplication application) {
    var firstName = application.getCandidateFirstName();
    var lastName = application.getCandidateLastName();
    var name = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    return name.isBlank() ? null : name;
  }
}
