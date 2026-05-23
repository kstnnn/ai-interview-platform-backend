package io.github.kstnnn.organization.service.repository;

import io.github.kstnnn.organization.service.model.OrganizationMember;
import io.github.kstnnn.organization.service.model.OrganizationMemberRole;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {

  boolean existsByOrganizationIdAndUserIdAndRoleIn(
      UUID organizationId, UUID userId, Collection<OrganizationMemberRole> roles);

  List<OrganizationMember> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
