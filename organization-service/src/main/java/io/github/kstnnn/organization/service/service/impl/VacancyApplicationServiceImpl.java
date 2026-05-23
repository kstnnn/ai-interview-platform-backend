package io.github.kstnnn.organization.service.service.impl;

import io.github.kstnnn.organization.service.dto.AiCustomQuestionRequest;
import io.github.kstnnn.organization.service.dto.AiStartInterviewRequest;
import io.github.kstnnn.organization.service.dto.VacancyApplicationResponse;
import io.github.kstnnn.organization.service.dto.VacancyApplyRequest;
import io.github.kstnnn.organization.service.exception.DuplicateApplicationException;
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
    if (vacancyApplicationRepository.existsByVacancyIdAndCandidateUserId(vacancyId, candidate.id())) {
      throw new DuplicateApplicationException();
    }
    var application =
        vacancyApplicationRepository.save(
            VacancyApplication.builder()
                .vacancy(vacancy)
                .candidateUserId(candidate.id())
                .coverLetter(request != null ? request.coverLetter() : null)
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
    return new VacancyApplicationResponse(
        application.getId(),
        application.getVacancy().getId(),
        application.getCandidateUserId(),
        application.getStatus(),
        application.getInterviewSessionId(),
        application.getCoverLetter(),
        application.getCreatedAt(),
        application.getUpdatedAt());
  }
}
