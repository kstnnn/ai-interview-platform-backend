package io.github.kstnnn.organization.service.service;

import io.github.kstnnn.organization.service.exception.AccessDeniedException;
import io.github.kstnnn.organization.service.model.OrganizationMemberRole;
import io.github.kstnnn.organization.service.repository.OrganizationMemberRepository;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationAccessService {

  private static final Collection<OrganizationMemberRole> WRITE_ROLES =
      java.util.List.of(
          OrganizationMemberRole.OWNER, OrganizationMemberRole.ADMIN, OrganizationMemberRole.RECRUITER);

  private final OrganizationMemberRepository organizationMemberRepository;

  public void requireWritableMember(UUID organizationId, UUID userId) {
    if (!organizationMemberRepository.existsByOrganizationIdAndUserIdAndRoleIn(
        organizationId, userId, WRITE_ROLES)) {
      throw new AccessDeniedException("User is not allowed to manage this organization");
    }
  }
}
