package io.github.kstnnn.organization.service.repository;

import io.github.kstnnn.organization.service.model.Organization;
import io.github.kstnnn.organization.service.model.OrganizationStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

  Optional<Organization> findByIdAndStatus(UUID id, OrganizationStatus status);
}
