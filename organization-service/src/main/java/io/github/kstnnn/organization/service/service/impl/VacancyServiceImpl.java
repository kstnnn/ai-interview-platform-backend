package io.github.kstnnn.organization.service.service.impl;

import io.github.kstnnn.organization.service.dto.PublicVacancyResponse;
import io.github.kstnnn.organization.service.dto.VacancyCreateRequest;
import io.github.kstnnn.organization.service.dto.VacancyResponse;
import io.github.kstnnn.organization.service.dto.VacancyUpdateRequest;
import io.github.kstnnn.organization.service.exception.InvalidVacancyStatusTransitionException;
import io.github.kstnnn.organization.service.exception.ResourceNotFoundException;
import io.github.kstnnn.organization.service.model.OrganizationStatus;
import io.github.kstnnn.organization.service.model.Vacancy;
import io.github.kstnnn.organization.service.model.VacancyStatus;
import io.github.kstnnn.organization.service.model.VacancyTechnology;
import io.github.kstnnn.organization.service.repository.OrganizationRepository;
import io.github.kstnnn.organization.service.repository.VacancyRepository;
import io.github.kstnnn.organization.service.repository.VacancyTechnologyRepository;
import io.github.kstnnn.organization.service.service.CurrentUserService;
import io.github.kstnnn.organization.service.service.OrganizationAccessService;
import io.github.kstnnn.organization.service.service.VacancyService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VacancyServiceImpl implements VacancyService {

  private final VacancyRepository vacancyRepository;
  private final VacancyTechnologyRepository vacancyTechnologyRepository;
  private final OrganizationRepository organizationRepository;
  private final CurrentUserService currentUserService;
  private final OrganizationAccessService organizationAccessService;

  @Override
  @Transactional
  public VacancyResponse create(Jwt jwt, UUID organizationId, VacancyCreateRequest request) {
    var user = currentUserService.requireActiveBusinessUser(jwt);
    organizationAccessService.requireWritableMember(organizationId, user.id());
    var organization =
        organizationRepository
            .findByIdAndStatus(organizationId, OrganizationStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    var vacancy =
        vacancyRepository.save(
            Vacancy.builder()
                .organization(organization)
                .title(request.title())
                .description(request.description())
                .requirements(request.requirements())
                .location(request.location())
                .employmentType(request.employmentType())
                .workFormat(request.workFormat())
                .level(request.level())
                .status(VacancyStatus.DRAFT)
                .createdByUserId(user.id())
                .build());
    replaceTechnologies(vacancy, request.technologyKeys());
    return toResponse(vacancy);
  }

  @Override
  @Transactional(readOnly = true)
  public List<VacancyResponse> getOrganizationVacancies(Jwt jwt, UUID organizationId) {
    var user = currentUserService.requireActiveBusinessUser(jwt);
    organizationAccessService.requireWritableMember(organizationId, user.id());
    return vacancyRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public VacancyResponse getById(Jwt jwt, UUID vacancyId) {
    var vacancy = loadVacancy(vacancyId);
    if (vacancy.getStatus() == VacancyStatus.PUBLISHED) {
      return toResponse(vacancy);
    }
    if (jwt == null) {
      throw new ResourceNotFoundException("Vacancy not found");
    }
    var user = currentUserService.requireActiveBusinessUser(jwt);
    organizationAccessService.requireWritableMember(vacancy.getOrganization().getId(), user.id());
    return toResponse(vacancy);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PublicVacancyResponse> getPublishedVacancies() {
    return vacancyRepository.findByStatusOrderByCreatedAtDesc(VacancyStatus.PUBLISHED).stream()
        .map(this::toPublicResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public PublicVacancyResponse getPublishedById(UUID vacancyId) {
    var vacancy = loadVacancy(vacancyId);
    if (vacancy.getStatus() != VacancyStatus.PUBLISHED) {
      throw new ResourceNotFoundException("Vacancy not found");
    }
    return toPublicResponse(vacancy);
  }

  @Override
  @Transactional
  public VacancyResponse update(Jwt jwt, UUID vacancyId, VacancyUpdateRequest request) {
    var vacancy = loadWritableVacancy(jwt, vacancyId);
    if (request.title() != null) {
      vacancy.setTitle(request.title());
    }
    if (request.description() != null) {
      vacancy.setDescription(request.description());
    }
    if (request.requirements() != null) {
      vacancy.setRequirements(request.requirements());
    }
    if (request.location() != null) {
      vacancy.setLocation(request.location());
    }
    if (request.employmentType() != null) {
      vacancy.setEmploymentType(request.employmentType());
    }
    if (request.workFormat() != null) {
      vacancy.setWorkFormat(request.workFormat());
    }
    if (request.level() != null) {
      vacancy.setLevel(request.level());
    }
    if (request.technologyKeys() != null) {
      replaceTechnologies(vacancy, request.technologyKeys());
    }
    return toResponse(vacancy);
  }

  @Override
  @Transactional
  public VacancyResponse draft(Jwt jwt, UUID vacancyId) {
    var vacancy = loadWritableVacancy(jwt, vacancyId);
    changeStatus(vacancy, VacancyStatus.DRAFT, VacancyStatus.PUBLISHED, VacancyStatus.DRAFT);
    return toResponse(vacancy);
  }

  @Override
  @Transactional
  public VacancyResponse publish(Jwt jwt, UUID vacancyId) {
    var vacancy = loadWritableVacancy(jwt, vacancyId);
    changeStatus(
        vacancy, VacancyStatus.PUBLISHED, VacancyStatus.DRAFT, VacancyStatus.CLOSED, VacancyStatus.PUBLISHED);
    return toResponse(vacancy);
  }

  @Override
  @Transactional
  public VacancyResponse close(Jwt jwt, UUID vacancyId) {
    var vacancy = loadWritableVacancy(jwt, vacancyId);
    changeStatus(vacancy, VacancyStatus.CLOSED, VacancyStatus.PUBLISHED, VacancyStatus.CLOSED);
    return toResponse(vacancy);
  }

  private void changeStatus(Vacancy vacancy, VacancyStatus targetStatus, VacancyStatus... allowedStatuses) {
    if (!java.util.Arrays.asList(allowedStatuses).contains(vacancy.getStatus())) {
      throw new InvalidVacancyStatusTransitionException(vacancy.getStatus(), targetStatus);
    }
    vacancy.setStatus(targetStatus);
  }

  private Vacancy loadWritableVacancy(Jwt jwt, UUID vacancyId) {
    var user = currentUserService.requireActiveBusinessUser(jwt);
    var vacancy = loadVacancy(vacancyId);
    organizationAccessService.requireWritableMember(vacancy.getOrganization().getId(), user.id());
    return vacancy;
  }

  private Vacancy loadVacancy(UUID vacancyId) {
    return vacancyRepository
        .findById(vacancyId)
        .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));
  }

  private void replaceTechnologies(Vacancy vacancy, List<String> technologyKeys) {
    vacancyTechnologyRepository.deleteByVacancyId(vacancy.getId());
    var technologies =
        normalizeTechnologyKeys(technologyKeys).stream()
            .map(key -> VacancyTechnology.builder().vacancy(vacancy).technologyKey(key).build())
            .toList();
    vacancyTechnologyRepository.saveAll(technologies);
  }

  private List<String> normalizeTechnologyKeys(List<String> technologyKeys) {
    if (technologyKeys == null) {
      return List.of();
    }
    return technologyKeys.stream()
        .map(String::trim)
        .map(String::toLowerCase)
        .filter(key -> !key.isBlank())
        .distinct()
        .toList();
  }

  private VacancyResponse toResponse(Vacancy vacancy) {
    return new VacancyResponse(
        vacancy.getId(),
        vacancy.getOrganization().getId(),
        vacancy.getOrganization().getName(),
        vacancy.getTitle(),
        vacancy.getDescription(),
        vacancy.getRequirements(),
        vacancy.getLocation(),
        vacancy.getEmploymentType(),
        vacancy.getWorkFormat(),
        vacancy.getLevel(),
        vacancy.getStatus(),
        vacancy.getCreatedByUserId(),
        vacancyTechnologyRepository.findTechnologyKeysByVacancyId(vacancy.getId()),
        vacancy.getCreatedAt(),
        vacancy.getUpdatedAt());
  }

  private PublicVacancyResponse toPublicResponse(Vacancy vacancy) {
    return new PublicVacancyResponse(
        vacancy.getId(),
        vacancy.getOrganization().getId(),
        vacancy.getOrganization().getName(),
        vacancy.getTitle(),
        vacancy.getDescription(),
        vacancy.getRequirements(),
        vacancy.getLocation(),
        vacancy.getEmploymentType(),
        vacancy.getWorkFormat(),
        vacancy.getLevel(),
        vacancyTechnologyRepository.findTechnologyKeysByVacancyId(vacancy.getId()),
        vacancy.getCreatedAt());
  }
}
