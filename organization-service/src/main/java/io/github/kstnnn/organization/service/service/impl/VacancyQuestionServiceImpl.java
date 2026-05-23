package io.github.kstnnn.organization.service.service.impl;

import io.github.kstnnn.organization.service.dto.VacancyQuestionRequest;
import io.github.kstnnn.organization.service.dto.VacancyQuestionResponse;
import io.github.kstnnn.organization.service.exception.ResourceNotFoundException;
import io.github.kstnnn.organization.service.model.VacancyQuestion;
import io.github.kstnnn.organization.service.repository.VacancyQuestionRepository;
import io.github.kstnnn.organization.service.repository.VacancyRepository;
import io.github.kstnnn.organization.service.service.CurrentUserService;
import io.github.kstnnn.organization.service.service.OrganizationAccessService;
import io.github.kstnnn.organization.service.service.VacancyQuestionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VacancyQuestionServiceImpl implements VacancyQuestionService {

  private final VacancyQuestionRepository vacancyQuestionRepository;
  private final VacancyRepository vacancyRepository;
  private final CurrentUserService currentUserService;
  private final OrganizationAccessService organizationAccessService;

  @Override
  @Transactional
  public VacancyQuestionResponse create(Jwt jwt, UUID vacancyId, VacancyQuestionRequest request) {
    var vacancy = loadWritableVacancy(jwt, vacancyId);
    var question =
        vacancyQuestionRepository.save(
            VacancyQuestion.builder()
                .vacancy(vacancy)
                .questionText(request.questionText())
                .expectedAnswer(request.expectedAnswer())
                .evaluationRubric(request.evaluationRubric())
                .topic(request.topic())
                .required(request.required() == null || request.required())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .active(true)
                .build());
    return toResponse(question);
  }

  @Override
  @Transactional(readOnly = true)
  public List<VacancyQuestionResponse> list(Jwt jwt, UUID vacancyId) {
    loadWritableVacancy(jwt, vacancyId);
    return vacancyQuestionRepository.findByVacancyIdAndActiveTrueOrderByDisplayOrderAsc(vacancyId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public VacancyQuestionResponse update(
      Jwt jwt, UUID vacancyId, UUID questionId, VacancyQuestionRequest request) {
    loadWritableVacancy(jwt, vacancyId);
    var question = loadQuestion(vacancyId, questionId);
    question.setQuestionText(request.questionText());
    question.setExpectedAnswer(request.expectedAnswer());
    question.setEvaluationRubric(request.evaluationRubric());
    question.setTopic(request.topic());
    question.setRequired(request.required() == null || request.required());
    question.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : question.getDisplayOrder());
    return toResponse(question);
  }

  @Override
  @Transactional
  public void delete(Jwt jwt, UUID vacancyId, UUID questionId) {
    loadWritableVacancy(jwt, vacancyId);
    var question = loadQuestion(vacancyId, questionId);
    question.setActive(false);
  }

  private io.github.kstnnn.organization.service.model.Vacancy loadWritableVacancy(Jwt jwt, UUID vacancyId) {
    var user = currentUserService.requireActiveBusinessUser(jwt);
    var vacancy =
        vacancyRepository
            .findById(vacancyId)
            .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));
    organizationAccessService.requireWritableMember(vacancy.getOrganization().getId(), user.id());
    return vacancy;
  }

  private VacancyQuestion loadQuestion(UUID vacancyId, UUID questionId) {
    var question =
        vacancyQuestionRepository
            .findById(questionId)
            .orElseThrow(() -> new ResourceNotFoundException("Vacancy question not found"));
    if (!question.getVacancy().getId().equals(vacancyId) || !question.isActive()) {
      throw new ResourceNotFoundException("Vacancy question not found");
    }
    return question;
  }

  private VacancyQuestionResponse toResponse(VacancyQuestion question) {
    return new VacancyQuestionResponse(
        question.getId(),
        question.getVacancy().getId(),
        question.getQuestionText(),
        question.getExpectedAnswer(),
        question.getEvaluationRubric(),
        question.getTopic(),
        question.isRequired(),
        question.getDisplayOrder(),
        question.isActive(),
        question.getCreatedAt(),
        question.getUpdatedAt());
  }
}
