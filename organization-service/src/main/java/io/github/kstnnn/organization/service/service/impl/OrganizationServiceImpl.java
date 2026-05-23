package io.github.kstnnn.organization.service.service.impl;

import io.github.kstnnn.organization.service.dto.OrganizationCreateRequest;
import io.github.kstnnn.organization.service.dto.OrganizationResponse;
import io.github.kstnnn.organization.service.dto.OrganizationUpdateRequest;
import io.github.kstnnn.organization.service.exception.ResourceNotFoundException;
import io.github.kstnnn.organization.service.model.Organization;
import io.github.kstnnn.organization.service.model.OrganizationMember;
import io.github.kstnnn.organization.service.model.OrganizationMemberRole;
import io.github.kstnnn.organization.service.model.OrganizationStatus;
import io.github.kstnnn.organization.service.repository.OrganizationMemberRepository;
import io.github.kstnnn.organization.service.repository.OrganizationRepository;
import io.github.kstnnn.organization.service.service.CurrentUserService;
import io.github.kstnnn.organization.service.service.OrganizationAccessService;
import io.github.kstnnn.organization.service.service.OrganizationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final OrganizationMemberRepository organizationMemberRepository;
  private final CurrentUserService currentUserService;
  private final OrganizationAccessService organizationAccessService;

  @Override
  @Transactional
  public OrganizationResponse create(Jwt jwt, OrganizationCreateRequest request) {
    var user = currentUserService.requireActiveBusinessUser(jwt);
    var organization =
        organizationRepository.save(
            Organization.builder()
                .ownerUserId(user.id())
                .name(request.name())
                .description(request.description())
                .websiteUrl(request.websiteUrl())
                .logoUrl(request.logoUrl())
                .status(OrganizationStatus.ACTIVE)
                .build());

    organizationMemberRepository.save(
        OrganizationMember.builder()
            .organization(organization)
            .userId(user.id())
            .role(OrganizationMemberRole.OWNER)
            .build());

    return toResponse(organization);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrganizationResponse> getMyOrganizations(Jwt jwt) {
    var user = currentUserService.requireActiveBusinessUser(jwt);
    return organizationMemberRepository.findByUserIdOrderByCreatedAtDesc(user.id()).stream()
        .map(OrganizationMember::getOrganization)
        .filter(organization -> organization.getStatus() == OrganizationStatus.ACTIVE)
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public OrganizationResponse getById(Jwt jwt, UUID organizationId) {
    var user = currentUserService.requireActiveBusinessUser(jwt);
    organizationAccessService.requireWritableMember(organizationId, user.id());
    return toResponse(loadActiveOrganization(organizationId));
  }

  @Override
  @Transactional
  public OrganizationResponse update(Jwt jwt, UUID organizationId, OrganizationUpdateRequest request) {
    var user = currentUserService.requireActiveBusinessUser(jwt);
    organizationAccessService.requireWritableMember(organizationId, user.id());
    var organization = loadActiveOrganization(organizationId);
    if (request.name() != null) {
      organization.setName(request.name());
    }
    if (request.description() != null) {
      organization.setDescription(request.description());
    }
    if (request.websiteUrl() != null) {
      organization.setWebsiteUrl(request.websiteUrl());
    }
    if (request.logoUrl() != null) {
      organization.setLogoUrl(request.logoUrl());
    }
    return toResponse(organization);
  }

  private Organization loadActiveOrganization(UUID organizationId) {
    return organizationRepository
        .findByIdAndStatus(organizationId, OrganizationStatus.ACTIVE)
        .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
  }

  private OrganizationResponse toResponse(Organization organization) {
    return new OrganizationResponse(
        organization.getId(),
        organization.getOwnerUserId(),
        organization.getName(),
        organization.getDescription(),
        organization.getWebsiteUrl(),
        organization.getLogoUrl(),
        organization.getStatus(),
        organization.getCreatedAt(),
        organization.getUpdatedAt());
  }
}
